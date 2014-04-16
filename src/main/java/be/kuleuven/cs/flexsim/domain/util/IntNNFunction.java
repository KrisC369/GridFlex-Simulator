package be.kuleuven.cs.flexsim.domain.util;

import javax.annotation.Nullable;

public interface IntNNFunction<F> {
    int apply(@Nullable F input);
}
