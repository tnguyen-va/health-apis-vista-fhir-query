package gov.va.api.health.vistafhirquery.service.controller.witnessprotection;

import gov.va.api.health.r4.api.resources.Resource;
import java.util.stream.Stream;

/**
 * To add witness protection for your resource type, create an implementation of this interface that
 * is available in the Spring context.
 *
 * <pre>{@code
 * @Component
 * public class WhateverWitnessProtectionAgent implements WitnessProtectionAgent&lt;Whatever> {
 *   void updateReference(Whatever whatever) {
 *     // do whatever you need here
 *   }
 * }
 * }</pre>
 *
 * @param <ResourceT>
 */
public interface WitnessProtectionAgent<ResourceT extends Resource> {
  Stream<ProtectedReference> referencesOf(ResourceT resource);
}
