package com.hcl.domino.commons.design.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.hcl.domino.data.IAdaptable;
import com.hcl.domino.design.format.CollateDescriptor;
import com.hcl.domino.design.format.CollateDescriptor.Flag;
import com.hcl.domino.design.format.Collation;

public class DominoCollationInfo implements IAdaptable {
  private Collation collation;
  private List<DominoCollateColumn> collateColumns;
  
  public DominoCollationInfo() {
    this.collation = Collation.newInstance();
    this.collateColumns = new ArrayList<>();
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public <T> T getAdapter(Class<T> clazz) {
    if (clazz == Collation.class) {
      return (T) this.collation;
    }
    else if (clazz == CollateDescriptor[].class) {
      if (this.collateColumns!=null) {
        return (T) this.collateColumns
            .stream()
            .map((col) -> {
              CollateDescriptor desc = col.getAdapter(CollateDescriptor.class);
              return desc;
            })
            .collect(Collectors.toList())
            .toArray(new CollateDescriptor[this.collateColumns.size()]);
      }
    }

    return null;
  }
  
  public void read(Collation collation) {
    this.collation = collation;
  }
  
  public void read(List<CollateDescriptor> collateDescriptors) {
    this.collateColumns = collateDescriptors
        .stream()
        .map((desc) -> {
          DominoCollateColumn col = new DominoCollateColumn();
          col.read(desc);
          return col;
        })
        .collect(Collectors.toList());
  }
  
  private Collation getCollation() {
    return this.collation;
  }
  
  /**
   * Flag to indicate only build on demand.
   * 
   * @return true for build on demand
   */
  public boolean isBuildOnDemand() {
    return getCollation().getFlags().contains(Collation.Flag.BUILD_ON_DEMAND);
  }

  public DominoCollationInfo setBuildOnDemand(boolean b) {
    this.collation.setFlag(Collation.Flag.BUILD_ON_DEMAND, b);
    return this;
  }

  /**
   * Indicates unique keys. Used for ODBC Access: Generate unique keys in index.
   * 
   * @return true for unique keys
   */
  public boolean isUnique() {
    return getCollation().getFlags().contains(Collation.Flag.UNIQUE);
  }

  public DominoCollationInfo setUnique(boolean b) {
    this.collation.setFlag(Collation.Flag.UNIQUE, b);
    return this;
  }
  
  public List<DominoCollateColumn> getColumns() {
    return Collections.unmodifiableList(Objects.requireNonNull(this.collateColumns, "Collate columns not found"));
  }
  
  public DominoCollateColumn addColumn(String name, boolean isDecending) {
    DominoCollateColumn newCol = new DominoCollateColumn();
    newCol.setName(name);
    newCol.setDescending(isDecending);
    this.collateColumns.add(newCol);
    return newCol;
  }
  
  public static class DominoCollateColumn implements IAdaptable {
    private CollateDescriptor collateDescriptor;
    
    public DominoCollateColumn() {
      this.collateDescriptor = CollateDescriptor.newInstance();
    }
    
    private void read(CollateDescriptor collateDescriptor) {
      this.collateDescriptor = collateDescriptor;
    }

    @Override
    public <T> T getAdapter(Class<T> clazz) {
      if (clazz == CollateDescriptor.class) {
        return (T) this.collateDescriptor;
      }

      return null;
    }
    
    private CollateDescriptor getCollateDescriptor() {
      return this.collateDescriptor;
    }
    
    public String getName() {
      return getCollateDescriptor().getName();
    }
    
    public DominoCollateColumn setName(String name) {
      this.collateDescriptor.setName(name);
      return this;
    }
    
    public boolean isDescending() {
      return getCollateDescriptor().getFlags().contains(CollateDescriptor.Flag.Descending);
    }
    
    public DominoCollateColumn setDescending(boolean b) {
      this.collateDescriptor.setFlag(Flag.Descending, b);
      return this;
    }
    
    public boolean isCaseSensitive() {
      return getCollateDescriptor().getFlags().contains(CollateDescriptor.Flag.CaseSensitiveInV5);
    }
    
    public DominoCollateColumn setCaseSensitive(boolean b) {
      this.collateDescriptor.setFlag(Flag.CaseSensitiveInV5, b);
      return this;
    }
    
    public boolean isAccentSensitive() {
      return getCollateDescriptor().getFlags().contains(CollateDescriptor.Flag.AccentSensitiveInV5);
    }
    
    public DominoCollateColumn setAccentSensitive(boolean b) {
      this.collateDescriptor.setFlag(Flag.AccentSensitiveInV5, b);
      return this;
    }
    
    public boolean isIgnorePrefixes() {
      return getCollateDescriptor().getFlags().contains(CollateDescriptor.Flag.IgnorePrefixes);
    }
    
    public DominoCollateColumn setIgnorePrefixes(boolean b) {
      this.collateDescriptor.setFlag(Flag.IgnorePrefixes, b);
      return this;
    }
    
    
  }
}
