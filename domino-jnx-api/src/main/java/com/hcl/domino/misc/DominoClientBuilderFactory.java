package com.hcl.domino.misc;

import java.util.function.Supplier;

import com.hcl.domino.DominoClientBuilder;

/**
 * Service type that can provide a {@link DominoClientBuilder} instance,
 * allowing the builder itself to be stateful while the factory is
 * stateless.
 * 
 * @author Jesse Gallagher
 * @since 1.19.1
 */
public interface DominoClientBuilderFactory extends Supplier<DominoClientBuilder> {

}
