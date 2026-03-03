package io.conditionals.condition.impl;

import io.conditionals.condition.ConditionalOnPortAvailable;
import io.conditionals.condition.utils.ConditionUtils;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.annotation.AnnotationAttributes;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.ServerSocket;
import java.util.Arrays;

/**
 * {@link org.springframework.boot.autoconfigure.condition.SpringBootCondition} implementation backing
 * {@link io.conditionals.condition.ConditionalOnPortAvailable}.
 *
 * <p>This condition matches when all configured ports can be bound using {@link ServerSocket}.
 * Evaluation probes each port by attempting to create a server socket, enabling {@link ServerSocket#setReuseAddress(boolean)}
 * and closing it immediately.</p>
 *
 * <p><b>Limitations</b></p>
 * <ul>
 *     <li>This check is inherently race-prone and should be treated as a best-effort diagnostic.</li>
 *     <li>Failure may be due to the port being in use, insufficient permissions, or OS/network configuration.</li>
 * </ul>
 *
 * <p><b>Thread safety</b></p>
 * <p>This type is stateless and thread-safe.</p>
 *
 * @author Maksym Uimanov
 * @since 1.0
 */
public class OnPortAvailableCondition extends MatchingSpringBootCondition {
    private static final String VALUE = "value";

    @Override
    protected Class<? extends Annotation> getAnnotationClass() {
        return ConditionalOnPortAvailable.class;
    }

    @Override
    protected ConditionOutcome determineOutcome(ConditionMessage.Builder message, ConditionContext context, AnnotationAttributes annotationAttributes) {
        int[] ports = (int[]) annotationAttributes.get(VALUE);
        boolean portAvailable = isPortAvailable(ports);
        return portAvailable
                ? ConditionOutcome.match()
                : ConditionUtils.noMatchBecause(message, "Port ", Arrays.toString(ports), " is not available");
    }

    private boolean isPortAvailable(int[] ports) {
        for (int port : ports) {
            try (ServerSocket socket = new ServerSocket(port)) {
                socket.setReuseAddress(true);
            } catch (IOException e) {
                return false;
            }
        }

        return true;
    }
}
