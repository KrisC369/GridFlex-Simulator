package be.kuleuven.cs.flexsim.domain.finances;

public enum DebtModel {
    CONSTANT {

        @Override
        public int calculateDebt(int timestep, int lastConsumption) {
            return lastConsumption;
        }

    };

    public abstract int calculateDebt(int timestep, int lastConsumption);

}
