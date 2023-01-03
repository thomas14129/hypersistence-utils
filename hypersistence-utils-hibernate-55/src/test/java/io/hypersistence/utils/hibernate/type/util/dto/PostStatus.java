package io.hypersistence.utils.hibernate.type.util.dto;

public enum PostStatus {
  PENDING, APPROVED, SPAM;

  @Override
  public String toString() {
    return String.format("The %s enum is mapped to ordinal: %d", name(), ordinal());
  }
}
