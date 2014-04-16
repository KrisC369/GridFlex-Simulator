package be.kuleuven.cs.flexsim.domain.finances;

import be.kuleuven.cs.flexsim.domain.resource.Resource;

public enum RewardModel {

    CONSTANT {

        @Override
        public int calculateReward(int timestep, Resource r) {
            return 5;
        }

    };

    public abstract int calculateReward(int timestep, Resource r);
}
