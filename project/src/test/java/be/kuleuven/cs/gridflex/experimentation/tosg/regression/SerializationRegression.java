package be.kuleuven.cs.gridflex.experimentation.tosg.regression;

import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.FlexAllocProblemContext;
import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.FlexProvider;
import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.FlexibilityProvider;
import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.HourlyFlexConstraints;
import be.kuleuven.cs.gridflex.domain.util.data.TimeSeries;
import be.kuleuven.cs.gridflex.domain.util.data.profiles.CongestionProfile;
import be.kuleuven.cs.gridflex.experimentation.tosg.wgmf.ExperimentParams;
import be.kuleuven.cs.gridflex.experimentation.tosg.wgmf.SerializationUtils;
import be.kuleuven.cs.gridflex.persistence.MapDBMemoizationContext;
import be.kuleuven.cs.gridflex.persistence.MemoizationContext;
import be.kuleuven.cs.gridflex.solvers.memoization.immutableViews.AllocResultsView;
import be.kuleuven.cs.gridflex.solvers.memoization.immutableViews.ImmutableSolverProblemContextView;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Base64;
import java.util.Collection;

import static be.kuleuven.cs.gridflex.experimentation.tosg.wgmf.SerializationUtils.unpickle;
import static be.kuleuven.cs.gridflex.experimentation.tosg.wgmf
        .WgmfGameRunnerVariableDistributionCostsTest.getParams;
import static be.kuleuven.cs.gridflex.experimentation.tosg.wgmf
        .WgmfGameRunnerVariableDistributionCostsTest.loadTestResources;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class SerializationRegression {
    private static final String DB_NAME = "DB";
    private HourlyFlexConstraints constr;
    private FlexibilityProvider provider1;
    private FlexibilityProvider provider2;
    private CongestionProfile congestionProfile;
    private FlexAllocProblemContext context;
    private Supplier<MemoizationContext<ImmutableSolverProblemContextView, AllocResultsView>>
            memoizationSupplier;
    private String HARDCODED_EXPECTED_CONTEXT_SERIALIZATION_PRE_REBRAND =
            "rO0ABXNyAGZiZS5rdWxldXZlbi5jcy5ncmlkZmxleC5zb2x2ZXJzLm1lbW9pemF0aW9uLmltbXV0YWJsZVZpZXdzLkF1dG9WYWx1ZV9JbW11dGFibGVTb2x2ZXJQcm9ibGVtQ29udGV4dFZpZXfTElJzXNNWTgIAA0oACXNlZWRWYWx1ZUwADGlucHV0UHJvZmlsZXQAKkxpdC91bmltaS9kc2kvZmFzdHV0aWwvZG91Ymxlcy9Eb3VibGVMaXN0O0wACXByb3ZpZGVyc3QAKUxjb20vZ29vZ2xlL2NvbW1vbi9jb2xsZWN0L0ltbXV0YWJsZUxpc3Q7eHIAXGJlLmt1bGV1dmVuLmNzLmdyaWRmbGV4LnNvbHZlcnMubWVtb2l6YXRpb24uaW1tdXRhYmxlVmlld3MuSW1tdXRhYmxlU29sdmVyUHJvYmxlbUNvbnRleHRWaWV30xJSc1zTVk4CAAB4cAAAAAAAAAAAc3IAOml0LnVuaW1pLmRzaS5mYXN0dXRpbC5kb3VibGVzLkRvdWJsZUxpc3RzJFVubW9kaWZpYWJsZUxpc3SeN3m5f0p8FwIAAUwABGxpc3RxAH4AAXhyAEZpdC51bmltaS5kc2kuZmFzdHV0aWwuZG91Ymxlcy5Eb3VibGVDb2xsZWN0aW9ucyRVbm1vZGlmaWFibGVDb2xsZWN0aW9unjd5uX9KfBcCAAFMAApjb2xsZWN0aW9udAAwTGl0L3VuaW1pL2RzaS9mYXN0dXRpbC9kb3VibGVzL0RvdWJsZUNvbGxlY3Rpb247eHBzcgAtaXQudW5pbWkuZHNpLmZhc3R1dGlsLmRvdWJsZXMuRG91YmxlQXJyYXlMaXN0njd5uX9KfBYDAAFJAARzaXpleHAAAAJXegAABAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA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AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB4cQB+AApzcgA2Y29tLmdvb2dsZS5jb21tb24uY29sbGVjdC5JbW11dGFibGVMaXN0JFNlcmlhbGl6ZWRGb3JtAAAAAAAAAAACAAFbAAhlbGVtZW50c3QAE1tMamF2YS9sYW5nL09iamVjdDt4cHVyABNbTGphdmEubGFuZy5PYmplY3Q7kM5YnxBzKWwCAAB4cAAAAAJzcgBcYmUua3VsZXV2ZW4uY3MuZ3JpZGZsZXguc29sdmVycy5tZW1vaXphdGlvbi5pbW11dGFibGVWaWV3cy5BdXRvVmFsdWVfRmxleGliaWxpdHlQcm92aWRlclZpZXeVEXD6ibkE8wIAAkwAIGZsZXhpYmlsaXR5QWN0aXZhdGlvbkNvbnN0cmFpbnRzdABGTGJlL2t1bGV1dmVuL2NzL2dyaWRmbGV4L2RvbWFpbi9lbmVyZ3kvZHNvL3IzZHAvSG91cmx5RmxleENvbnN0cmFpbnRzO0wAGWZsZXhpYmlsaXR5QWN0aXZhdGlvblJhdGV0AERMYmUva3VsZXV2ZW4vY3MvZ3JpZGZsZXgvZG9tYWluL3V0aWwvZGF0YS9Eb3VibGVQb3dlckNhcGFiaWxpdHlCYW5kO3hyAFJiZS5rdWxldXZlbi5jcy5ncmlkZmxleC5zb2x2ZXJzLm1lbW9pemF0aW9uLmltbXV0YWJsZVZpZXdzLkZsZXhpYmlsaXR5UHJvdmlkZXJWaWV3lRFw+om5BPMCAAB4cHNyAE5iZS5rdWxldXZlbi5jcy5ncmlkZmxleC5kb21haW4uZW5lcmd5LmRzby5yM2RwLkF1dG9WYWx1ZV9Ib3VybHlGbGV4Q29uc3RyYWludHPedmVxCEjMmwIAA0QAEmFjdGl2YXRpb25EdXJhdGlvbkQAE2ludGVyQWN0aXZhdGlvblRpbWVEABJtYXhpbXVtQWN0aXZhdGlvbnN4cgBEYmUua3VsZXV2ZW4uY3MuZ3JpZGZsZXguZG9tYWluLmVuZXJneS5kc28ucjNkcC5Ib3VybHlGbGV4Q29uc3RyYWludHPedmVxCEjMmwIAAHhwP/AAAAAAAABAAAAAAAAAAEAQAAAAAAAAc3IATGJlLmt1bGV1dmVuLmNzLmdyaWRmbGV4LmRvbWFpbi51dGlsLmRhdGEuQXV0b1ZhbHVlX0RvdWJsZVBvd2VyQ2FwYWJpbGl0eUJhbmTCl4LkxAbeLwIAAkQABGRvd25EAAJ1cHhyAEJiZS5rdWxldXZlbi5jcy5ncmlkZmxleC5kb21haW4udXRpbC5kYXRhLkRvdWJsZVBvd2VyQ2FwYWJpbGl0eUJhbmTCl4LkxAbeLwIAAHhwAAAAAAAAAABAaQAAAAAAAHNxAH4AEHEAfgAXc3EAfgAYAAAAAAAAAABAiQAAAAAAAA==";
    private String HARDCODED_EXPECTED_CONTEXT_SERIALIZATION_POST_REBRAND =
            "rO0ABXNyAGZiZS5rdWxldXZlbi5jcy5ncmlkZmxleC5zb2x2ZXJzLm1lbW9pemF0aW9uLmltbXV0YWJsZVZpZXdzLkF1dG9WYWx1ZV9JbW11dGFibGVTb2x2ZXJQcm9ibGVtQ29udGV4dFZpZXfTElJzXNNWTgIAA0oACXNlZWRWYWx1ZUwADGlucHV0UHJvZmlsZXQAKkxpdC91bmltaS9kc2kvZmFzdHV0aWwvZG91Ymxlcy9Eb3VibGVMaXN0O0wACXByb3ZpZGVyc3QAKUxjb20vZ29vZ2xlL2NvbW1vbi9jb2xsZWN0L0ltbXV0YWJsZUxpc3Q7eHIAXGJlLmt1bGV1dmVuLmNzLmdyaWRmbGV4LnNvbHZlcnMubWVtb2l6YXRpb24uaW1tdXRhYmxlVmlld3MuSW1tdXRhYmxlU29sdmVyUHJvYmxlbUNvbnRleHRWaWV30xJSc1zTVk4CAAB4cAAAAAAAAAAAc3IAOml0LnVuaW1pLmRzaS5mYXN0dXRpbC5kb3VibGVzLkRvdWJsZUxpc3RzJFVubW9kaWZpYWJsZUxpc3SeN3m5f0p8FwIAAUwABGxpc3RxAH4AAXhyAEZpdC51bmltaS5kc2kuZmFzdHV0aWwuZG91Ymxlcy5Eb3VibGVDb2xsZWN0aW9ucyRVbm1vZGlmaWFibGVDb2xsZWN0aW9unjd5uX9KfBcCAAFMAApjb2xsZWN0aW9udAAwTGl0L3VuaW1pL2RzaS9mYXN0dXRpbC9kb3VibGVzL0RvdWJsZUNvbGxlY3Rpb247eHBzcgAtaXQudW5pbWkuZHNpLmZhc3R1dGlsLmRvdWJsZXMuRG91YmxlQXJyYXlMaXN0njd5uX9KfBYDAAFJAARzaXpleHAAAAKeegAABAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA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aWRlclZpZXeVEXD6ibkE8wIAAkwAIGZsZXhpYmlsaXR5QWN0aXZhdGlvbkNvbnN0cmFpbnRzdABGTGJlL2t1bGV1dmVuL2NzL2dyaWRmbGV4L2RvbWFpbi9lbmVyZ3kvZHNvL3IzZHAvSG91cmx5RmxleENvbnN0cmFpbnRzO0wAGWZsZXhpYmlsaXR5QWN0aXZhdGlvblJhdGV0AERMYmUva3VsZXV2ZW4vY3MvZ3JpZGZsZXgvZG9tYWluL3V0aWwvZGF0YS9Eb3VibGVQb3dlckNhcGFiaWxpdHlCYW5kO3hyAFJiZS5rdWxldXZlbi5jcy5ncmlkZmxleC5zb2x2ZXJzLm1lbW9pemF0aW9uLmltbXV0YWJsZVZpZXdzLkZsZXhpYmlsaXR5UHJvdmlkZXJWaWV3lRFw+om5BPMCAAB4cHNyAE5iZS5rdWxldXZlbi5jcy5ncmlkZmxleC5kb21haW4uZW5lcmd5LmRzby5yM2RwLkF1dG9WYWx1ZV9Ib3VybHlGbGV4Q29uc3RyYWludHPedmVxCEjMmwIAA0QAEmFjdGl2YXRpb25EdXJhdGlvbkQAE2ludGVyQWN0aXZhdGlvblRpbWVEABJtYXhpbXVtQWN0aXZhdGlvbnN4cgBEYmUua3VsZXV2ZW4uY3MuZ3JpZGZsZXguZG9tYWluLmVuZXJneS5kc28ucjNkcC5Ib3VybHlGbGV4Q29uc3RyYWludHPedmVxCEjMmwIAAHhwP/AAAAAAAABAAAAAAAAAAEAQAAAAAAAAc3IATGJlLmt1bGV1dmVuLmNzLmdyaWRmbGV4LmRvbWFpbi51dGlsLmRhdGEuQXV0b1ZhbHVlX0RvdWJsZVBvd2VyQ2FwYWJpbGl0eUJhbmTCl4LkxAbeLwIAAkQABGRvd25EAAJ1cHhyAEJiZS5rdWxldXZlbi5jcy5ncmlkZmxleC5kb21haW4udXRpbC5kYXRhLkRvdWJsZVBvd2VyQ2FwYWJpbGl0eUJhbmTCl4LkxAbeLwIAAHhwAAAAAAAAAABAaQAAAAAAAHNxAH4AEHEAfgAXc3EAfgAYAAAAAAAAAABAiQAAAAAAAA==";

    @Before
    public void setUp() {
        constr = HourlyFlexConstraints.builder().activationDuration(1)
                .interActivationTime(2).maximumActivations(4).build();
        provider1 = new FlexProvider(200, constr);
        provider2 = new FlexProvider(800, constr);
        ExperimentParams params = getParams("DUMMY");
        congestionProfile = loadTestResources(params).getInputData()
                .getCongestionProfile();
        context = new FlexAllocProblemContext() {
            @Override
            public Collection<FlexibilityProvider> getProviders() {
                return Lists.newArrayList(provider1, provider2);
            }

            @Override
            public TimeSeries getEnergyProfileToMinimizeWithFlex() {
                return congestionProfile;
            }
        };
        memoizationSupplier = () -> MapDBMemoizationContext.builder().setFileName(DB_NAME)
                .ensureFileExists()
                .build();
    }

    @Test
    public void testDeserializeContextPostRebrand() throws IOException, ClassNotFoundException {
        byte[] decoded = Base64.getDecoder().decode(
                HARDCODED_EXPECTED_CONTEXT_SERIALIZATION_POST_REBRAND);
        ImmutableSolverProblemContextView unpickled = unpickle(decoded,
                ImmutableSolverProblemContextView.class);
        assertEquals(ImmutableSolverProblemContextView.from(context), unpickled);
    }

    @Test
    public void testSerializeContextPostRebrand() throws IOException {
        ImmutableSolverProblemContextView contextV = ImmutableSolverProblemContextView
                .from(context);
        byte[] pickled = SerializationUtils.pickle(contextV);
        String pickledAsb64String = Base64.getEncoder().encodeToString(pickled);
        System.out.println(pickledAsb64String);
        byte[] decoded = Base64.getDecoder().decode(pickledAsb64String);
        assertEquals(HARDCODED_EXPECTED_CONTEXT_SERIALIZATION_POST_REBRAND, pickledAsb64String);
        assertArrayEquals(pickled, decoded);
    }
}
