package be.kuleuven.cs.flexsim.domain.util.listener;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ListenerTest {
    Listener<? super Integer> test = NoopListener.INSTANCE;

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testInit() {
        Listener<? super Integer> test2 = NoopListener.INSTANCE;
        test2.eventOccurred(0);
        assertNotNull(test2);
    }

    @Test
    public void testPlus() {
        int arg = 4;
        Listener<Integer> mock = Mockito.mock(Listener.class);
        test = MultiplexListener.plus(test, mock);
        test.eventOccurred(arg);
        verify(mock, times(1)).eventOccurred(arg);
        Listener<Integer> mock2 = Mockito.mock(Listener.class);
        test = MultiplexListener.plus(test, mock2);
        test.eventOccurred(arg);
        verify(mock, times(2)).eventOccurred(arg);
        verify(mock2, times(1)).eventOccurred(arg);
        test = MultiplexListener.plus(test, NoopListener.INSTANCE);
        test.eventOccurred(arg);
        verify(mock, times(3)).eventOccurred(arg);
        verify(mock2, times(2)).eventOccurred(arg);
    }
}
