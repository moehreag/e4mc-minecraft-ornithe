/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.bootstrap;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.hash.Hashing;
import io.netty.channel.*;
import io.netty.resolver.AddressResolver;
import io.netty.resolver.AddressResolverGroup;
import io.netty.resolver.DefaultAddressResolverGroup;
import io.netty.resolver.NameResolver;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.internal.EmptyArrays;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.apache.commons.io.IOUtils;

/**
 * A {@link Bootstrap} that makes it easy to bootstrap a {@link Channel} to use
 * for clients.
 *
 * <p>The {@link #bind()} methods are useful in combination with connectionless transports such as datagram (UDP).
 * For regular TCP connections, please use the provided {@link #connect()} methods.</p>
 */
public final class Bootstrap extends AbstractBootstrap<Bootstrap, Channel> {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(Bootstrap.class);

	private static final AddressResolverGroup<?> DEFAULT_RESOLVER = DefaultAddressResolverGroup.INSTANCE;

	private final BootstrapConfig config = new BootstrapConfig(this);

	@SuppressWarnings("unchecked")
	private volatile AddressResolverGroup<SocketAddress> resolver =
			(AddressResolverGroup<SocketAddress>) DEFAULT_RESOLVER;
	private volatile SocketAddress remoteAddress;

	public Bootstrap() {
	}

	private Bootstrap(Bootstrap bootstrap) {
		super(bootstrap);
		resolver = bootstrap.resolver;
		remoteAddress = bootstrap.remoteAddress;
	}

	/**
	 * Sets the {@link NameResolver} which will resolve the address of the unresolved named address.
	 *
	 * @param resolver the {@link NameResolver} for this {@code Bootstrap}; may be {@code null}, in which case a default
	 *                 resolver will be used
	 * @see io.netty.resolver.DefaultAddressResolverGroup
	 */
	@SuppressWarnings("unchecked")
	public Bootstrap resolver(AddressResolverGroup<?> resolver) {
		this.resolver = (AddressResolverGroup<SocketAddress>) (resolver == null ? DEFAULT_RESOLVER : resolver);
		return this;
	}

	/**
	 * The {@link SocketAddress} to connect to once the {@link #connect()} method
	 * is called.
	 */
	public Bootstrap remoteAddress(SocketAddress remoteAddress) {
		this.remoteAddress = remoteAddress;
		return this;
	}

	/**
	 * @see #remoteAddress(SocketAddress)
	 */
	public Bootstrap remoteAddress(String inetHost, int inetPort) {
		remoteAddress = InetSocketAddress.createUnresolved(inetHost, inetPort);
		return this;
	}

	/**
	 * @see #remoteAddress(SocketAddress)
	 */
	public Bootstrap remoteAddress(InetAddress inetHost, int inetPort) {
		remoteAddress = new InetSocketAddress(inetHost, inetPort);
		return this;
	}

	/**
	 * Connect a {@link Channel} to the remote peer.
	 */
	public ChannelFuture connect() {
		validate();
		SocketAddress remoteAddress = this.remoteAddress;
		if (remoteAddress == null) {
			throw new IllegalStateException("remoteAddress not set");
		}

		return doResolveAndConnect(remoteAddress, config.localAddress());
	}

	/**
	 * Connect a {@link Channel} to the remote peer.
	 */
	public ChannelFuture connect(String inetHost, int inetPort) {
		return connect(InetSocketAddress.createUnresolved(inetHost, inetPort));
	}

	/**
	 * Connect a {@link Channel} to the remote peer.
	 */
	public ChannelFuture connect(InetAddress inetHost, int inetPort) {
		return connect(new InetSocketAddress(inetHost, inetPort));
	}

	/**
	 * Connect a {@link Channel} to the remote peer.
	 */
	public ChannelFuture connect(SocketAddress remoteAddress) {
		if (remoteAddress == null) {
			throw new NullPointerException("remoteAddress");
		}

		validate();
		return doResolveAndConnect(remoteAddress, config.localAddress());
	}

	/**
	 * Connect a {@link Channel} to the remote peer.
	 */
	public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress) {
		if (remoteAddress == null) {
			throw new NullPointerException("remoteAddress");
		}
		validate();
		return doResolveAndConnect(remoteAddress, localAddress);
	}

	/**
	 * @see #connect()
	 */
	private ChannelFuture doResolveAndConnect(final SocketAddress remoteAddress, final SocketAddress localAddress) {
		final ChannelFuture future = checkAddress(remoteAddress);
		if (future != null) {
			return future;
		}

		final ChannelFuture regFuture = initAndRegister();
		final Channel channel = regFuture.channel();

		if (regFuture.isDone()) {
			if (!regFuture.isSuccess()) {
				return regFuture;
			}
			return doResolveAndConnect0(channel, remoteAddress, localAddress, channel.newPromise());
		} else {
			// Registration future is almost always fulfilled already, but just in case it's not.
			final PendingRegistrationPromise promise = new PendingRegistrationPromise(channel);
			regFuture.addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					// Direclty obtain the cause and do a null check so we only need one volatile read in case of a
					// failure.
					Throwable cause = future.cause();
					if (cause != null) {
						// Registration on the EventLoop failed so fail the ChannelPromise directly to not cause an
						// IllegalStateException once we try to access the EventLoop of the Channel.
						promise.setFailure(cause);
					} else {
						// Registration was successful, so set the correct executor to use.
						// See https://github.com/netty/netty/issues/2586
						promise.registered();
						doResolveAndConnect0(channel, remoteAddress, localAddress, promise);
					}
				}
			});
			return promise;
		}
	}

	private ChannelFuture doResolveAndConnect0(final Channel channel, SocketAddress remoteAddress,
											   final SocketAddress localAddress, final ChannelPromise promise) {
		try {
			final EventLoop eventLoop = channel.eventLoop();
			final AddressResolver<SocketAddress> resolver = this.resolver.getResolver(eventLoop);

			if (!resolver.isSupported(remoteAddress) || resolver.isResolved(remoteAddress)) {
				// Resolver has no idea about what to do with the specified remote address or it's resolved already.
				doConnect(remoteAddress, localAddress, promise);
				return promise;
			}

			final Future<SocketAddress> resolveFuture = resolver.resolve(remoteAddress);

			if (resolveFuture.isDone()) {
				final Throwable resolveFailureCause = resolveFuture.cause();

				if (resolveFailureCause != null) {
					// Failed to resolve immediately
					channel.close();
					promise.setFailure(resolveFailureCause);
				} else {
					// Succeeded to resolve immediately; cached? (or did a blocking lookup)
					doConnect(resolveFuture.getNow(), localAddress, promise);
				}
				return promise;
			}

			// Wait until the name resolution is finished.
			resolveFuture.addListener(new FutureListener<SocketAddress>() {
				@Override
				public void operationComplete(Future<SocketAddress> future) throws Exception {
					if (future.cause() != null) {
						channel.close();
						promise.setFailure(future.cause());
					} else {
						doConnect(future.getNow(), localAddress, promise);
					}
				}
			});
		} catch (Throwable cause) {
			promise.tryFailure(cause);
		}
		return promise;
	}

	private static void doConnect(
			final SocketAddress remoteAddress, final SocketAddress localAddress, final ChannelPromise connectPromise) {

		// This method is invoked before channelRegistered() is triggered.  Give user handlers a chance to set up
		// the pipeline in its channelRegistered() implementation.
		final Channel channel = connectPromise.channel();
		channel.eventLoop().execute(() -> {
			if (localAddress == null) {
				channel.connect(remoteAddress, connectPromise);
			} else {
				channel.connect(remoteAddress, localAddress, connectPromise);
			}
			connectPromise.addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	void init(Channel channel) {
		ChannelPipeline p = channel.pipeline();
		p.addLast(config.handler());

		final Map<ChannelOption<?>, Object> options = options0();
		synchronized (options) {
			setChannelOptions(channel, newOptionsArray(options), logger);
		}

		final Map<AttributeKey<?>, Object> attrs = attrs0();
		synchronized (attrs) {
			for (Entry<AttributeKey<?>, Object> e : attrs.entrySet()) {
				channel.attr((AttributeKey<Object>) e.getKey()).set(e.getValue());
			}
		}
	}

	@Override
	public Bootstrap validate() {
		super.validate();
		if (config.handler() == null) {
			throw new IllegalStateException("handler not set");
		}
		return this;
	}

	@Override
	public Bootstrap clone() {
		return new Bootstrap(this);
	}

	/**
	 * Returns a deep clone of this bootstrap which has the identical configuration except that it uses
	 * the given {@link EventLoopGroup}. This method is useful when making multiple {@link Channel}s with similar
	 * settings.
	 */
	public Bootstrap clone(EventLoopGroup group) {
		Bootstrap bs = new Bootstrap(this);
		bs.group = group;
		return bs;
	}

	@Override
	public BootstrapConfig config() {
		return config;
	}

	SocketAddress remoteAddress() {
		return remoteAddress;
	}

	AddressResolverGroup<?> resolver() {
		return resolver;
	}

	private static final Joiner DOT_JOINER = Joiner.on('.');
	private static final Splitter DOT_SPLITTER = Splitter.on('.');

	@VisibleForTesting
	static final Set<String> BLOCKED_SERVERS = Sets.newHashSet();

	static {
		try {
			BLOCKED_SERVERS.addAll(IOUtils.readLines(new URL("https://sessionserver.mojang.com/blockedservers").openConnection().getInputStream()));
		} catch (IOException ignored) {
		}
	}

	@VisibleForTesting
	ChannelFuture checkAddress(SocketAddress remoteAddress) {
		if (remoteAddress instanceof InetSocketAddress inetSocketAddress) {
			final InetAddress address = inetSocketAddress.getAddress();

			final boolean isBlocked;
			if (address == null) {
				isBlocked = isBlockedServer(inetSocketAddress.getHostString());
			} else {
				isBlocked = isBlockedServer(address.getHostAddress()) || isBlockedServer(address.getHostName());
			}

			if (isBlocked) {
				final Channel channel = channelFactory().newChannel();
				channel.unsafe().closeForcibly();
				final SocketException cause = new SocketException("Network is unreachable");
				cause.setStackTrace(EmptyArrays.EMPTY_STACK_TRACE);
				return new DefaultChannelPromise(channel, GlobalEventExecutor.INSTANCE).setFailure(cause);
			}
		}
		return null;
	}

	public boolean isBlockedServer(String server) {
		if (server == null || server.isEmpty()) {
			return false;
		}

		while (server.charAt(server.length() - 1) == '.') {
			server = server.substring(0, server.length() - 1);
		}

		if (isBlockedServerHostName(server)) {
			return true;
		}

		final List<String> strings = Lists.newArrayList(DOT_SPLITTER.split(server));

		boolean isIp = strings.size() == 4;
		if (isIp) {
			for (final String string : strings) {
				try {
					final int part = Integer.parseInt(string);
					if (part >= 0 && part <= 255) {
						continue;
					}
				} catch (final NumberFormatException ignored) {
				}

				isIp = false;
				break;
			}
		}

		if (!isIp && isBlockedServerHostName("*." + server)) {
			return true;
		}

		while (strings.size() > 1) {
			strings.remove(isIp ? strings.size() - 1 : 0);

			final String starredPart = isIp ? DOT_JOINER.join(strings) + ".*" : "*." + DOT_JOINER.join(strings);
			if (isBlockedServerHostName(starredPart)) {
				return true;
			}
		}

		return false;
	}

	@SuppressWarnings("UnstableApiUsage")
	private boolean isBlockedServerHostName(final String server) {
		return BLOCKED_SERVERS.contains(Hashing.sha1().hashBytes(server.toLowerCase().getBytes(StandardCharsets.ISO_8859_1)).toString());
	}
}
