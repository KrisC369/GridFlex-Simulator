package be.kuleuven.cs.flexsim.domain.util;

import javax.annotation.Nullable;

public interface LongNNFunction<F> {
    long apply(@Nullable F input);
}
