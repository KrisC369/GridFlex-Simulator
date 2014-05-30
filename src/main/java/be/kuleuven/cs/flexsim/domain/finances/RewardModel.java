package be.kuleuven.cs.flexsim.domain.finances;

import be.kuleuven.cs.flexsim.domain.resource.Resource;

/**
 * The Reward model representation.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 * 
 */
public enum RewardModel {
    /**
     * Specifies a constant reward per unit of finished product.
     */
    CONSTANT {

        @Override
        public int calculateReward(int timestep, Resource r) {
            return REWARD_RATE;
        }

    };
    private static final int REWARD_RATE = 8000;

    /**
     * Calculate a reward for finishing a certain resource at timestep r.
     * 
     * @param timestep
     *            The timestep.
     * @param r
     *            The finished resource.
     * @return The reward amount.
     */
    public abstract int calculateReward(int timestep, Resource r);
}
