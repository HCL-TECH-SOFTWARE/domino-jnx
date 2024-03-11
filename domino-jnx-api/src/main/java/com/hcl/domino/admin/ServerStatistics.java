package com.hcl.domino.admin;

import java.util.Collection;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.NotesConstants;

public interface ServerStatistics {
  enum Flag implements INumberEnum<Short> {
    UNIQUE(NotesConstants.ST_UNIQUE),
    ADDITIVE(NotesConstants.ST_ADDITIVE),
    RESETABLE(NotesConstants.ST_RESETABLE);
    private final short value;
    private Flag(short value) {
      this.value = value;
    }
    
    @Override
    public long getLongValue() {
      return value;
    }
    @Override
    public Short getValue() {
      return value;
    }
  }
  
  /**
   * Updates or creates a statistic with an {@code int} value.
   * 
   * @param facility the name of the facility housing the statistic
   * @param statName the name of the statistic
   * @param flags {@link Flag}s to apply to the statistic
   * @param value the new value for the statistic
   */
  void updateStatistic(String facility, String statName, Collection<Flag> flags, int value);
  
  /**
   * Updates or creates a statistic with an {@code String} value.
   * 
   * @param facility the name of the facility housing the statistic
   * @param statName the name of the statistic
   * @param flags {@link Flag}s to apply to the statistic
   * @param value the new value for the statistic
   */
  void updateStatistic(String facility, String statName, Collection<Flag> flags, String value);
  
  /**
   * Updates or creates a statistic with an {@code double} value.
   * 
   * @param facility the name of the facility housing the statistic
   * @param statName the name of the statistic
   * @param flags {@link Flag}s to apply to the statistic
   * @param value the new value for the statistic
   */
  void updateStatistic(String facility, String statName, Collection<Flag> flags, double value);
  
  /**
   * Resets an existing statistic that was created with {@link Flag#RESETABLE}.
   * 
   * @param facility the name of the facility housing the statistic
   * @param statName the name of the statistic
   */
  void resetStatistic(String facility, String statName);
  
  /**
   * Resets an existing statistic.
   * 
   * @param facility the name of the facility housing the statistic
   * @param statName the name of the statistic
   */
  void deleteStatistic(String facility, String statName);
}
