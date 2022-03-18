/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package wiki.ganhua.wallet.tron.crypto.tuwenitypes;

import java.math.BigInteger;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * An unsigned 256-bit precision number.
 *
 * This is a raw {@link UInt256Value} - a 256-bit precision unsigned number of no particular unit.
 */
public final class UInt256 implements UInt256Value<UInt256> {
  private final static int MAX_CONSTANT = 64;
  private final static BigInteger BI_MAX_CONSTANT = BigInteger.valueOf(MAX_CONSTANT);
  private static UInt256[] CONSTANTS = new UInt256[MAX_CONSTANT + 1];
  static {
    CONSTANTS[0] = new UInt256(Bytes32.ZERO);
    for (int i = 1; i <= MAX_CONSTANT; ++i) {
      CONSTANTS[i] = new UInt256(i);
    }
  }

  /** The minimum value of a UInt256 */
  public final static UInt256 MIN_VALUE = valueOf(0);
  /** The maximum value of a UInt256 */
  public final static UInt256 MAX_VALUE = new UInt256(Bytes32.ZERO.not());
  /** The value 0 */
  public final static UInt256 ZERO = valueOf(0);
  /** The value 1 */
  public final static UInt256 ONE = valueOf(1);

  private static final int INTS_SIZE = 32 / 4;
  // The mask is used to obtain the value of an int as if it were unsigned.
  private static final long LONG_MASK = 0xFFFFFFFFL;
  private static final BigInteger P_2_256 = BigInteger.valueOf(2).pow(256);

  // The unsigned int components of the value
  private final int[] ints;

  /**
   * Return a {@code UInt256} containing the specified value.
   *
   * @param value The value to create a {@code UInt256} for.
   * @return A {@code UInt256} containing the specified value.
   * @throws IllegalArgumentException If the value is negative.
   */
  public static UInt256 valueOf(long value) {
    checkArgument(value >= 0, "Argument must be positive");
    if (value <= MAX_CONSTANT) {
      return CONSTANTS[(int) value];
    }
    return new UInt256(value);
  }

  /**
   * Return a {@link UInt256} containing the specified value.
   *
   * @param value the value to create a {@link UInt256} for
   * @return a {@link UInt256} containing the specified value
   * @throws IllegalArgumentException if the value is negative or too large to be represented as a UInt256
   */
  public static UInt256 valueOf(BigInteger value) {
    checkArgument(value.signum() >= 0, "Argument must be positive");
    checkArgument(value.bitLength() <= 256, "Argument is too large to represent a UInt256");
    if (value.compareTo(BI_MAX_CONSTANT) <= 0) {
      return CONSTANTS[value.intValue()];
    }
    int[] ints = new int[INTS_SIZE];
    for (int i = INTS_SIZE - 1; i >= 0; --i) {
      ints[i] = value.intValue();
      value = value.shiftRight(32);
    }
    return new UInt256(ints);
  }

  private UInt256(Bytes32 bytes) {
    this.ints = new int[INTS_SIZE];
    for (int i = 0, j = 0; i < INTS_SIZE; ++i, j += 4) {
      ints[i] = bytes.getInt(j);
    }
  }

  private UInt256(long value) {
    this.ints = new int[INTS_SIZE];
    this.ints[INTS_SIZE - 2] = (int) ((value >>> 32) & LONG_MASK);
    this.ints[INTS_SIZE - 1] = (int) (value & LONG_MASK);
  }

  private UInt256(int[] ints) {
    this.ints = ints;
  }

  @SuppressWarnings("ReferenceEquality")
  @Override
  public boolean isZero() {
    if (this == ZERO) {
      return true;
    }
    for (int i = INTS_SIZE - 1; i >= 0; --i) {
      if (this.ints[i] != 0) {
        return false;
      }
    }
    return true;
  }

  @Override
  public UInt256 add(UInt256 value) {
    if (value.isZero()) {
      return this;
    }
    if (isZero()) {
      return value;
    }
    int[] result = new int[INTS_SIZE];
    boolean constant = true;
    long sum = (this.ints[INTS_SIZE - 1] & LONG_MASK) + (value.ints[INTS_SIZE - 1] & LONG_MASK);
    result[INTS_SIZE - 1] = (int) (sum & LONG_MASK);
    if (result[INTS_SIZE - 1] < 0 || result[INTS_SIZE - 1] > MAX_CONSTANT) {
      constant = false;
    }
    for (int i = INTS_SIZE - 2; i >= 0; --i) {
      sum = (this.ints[i] & LONG_MASK) + (value.ints[i] & LONG_MASK) + (sum >>> 32);
      result[i] = (int) (sum & LONG_MASK);
      constant &= result[i] == 0;
    }
    if (constant) {
      return CONSTANTS[result[INTS_SIZE - 1]];
    }
    return new UInt256(result);
  }

  @Override
  public UInt256 add(long value) {
    if (value == 0) {
      return this;
    }
    if (value > 0 && isZero()) {
      return UInt256.valueOf(value);
    }
    int[] result = new int[INTS_SIZE];
    boolean constant = true;
    long sum = (this.ints[INTS_SIZE - 1] & LONG_MASK) + (value & LONG_MASK);
    result[INTS_SIZE - 1] = (int) (sum & LONG_MASK);
    if (result[INTS_SIZE - 1] < 0 || result[INTS_SIZE - 1] > MAX_CONSTANT) {
      constant = false;
    }
    sum = (this.ints[INTS_SIZE - 2] & LONG_MASK) + (value >>> 32) + (sum >>> 32);
    result[INTS_SIZE - 2] = (int) (sum & LONG_MASK);
    constant &= result[INTS_SIZE - 2] == 0;
    long signExtent = (value >> 63) & LONG_MASK;
    for (int i = INTS_SIZE - 3; i >= 0; --i) {
      sum = (this.ints[i] & LONG_MASK) + signExtent + (sum >>> 32);
      result[i] = (int) (sum & LONG_MASK);
      constant &= result[i] == 0;
    }
    if (constant) {
      return CONSTANTS[result[INTS_SIZE - 1]];
    }
    return new UInt256(result);
  }
  /**
   * Return a bit-wise AND of this value and the supplied value.
   *
   * @param value the value to perform the operation with
   * @return the result of a bit-wise AND
   */
  public UInt256 and(UInt256 value) {
    int[] result = new int[INTS_SIZE];
    for (int i = INTS_SIZE - 1; i >= 0; --i) {
      result[i] = this.ints[i] & value.ints[i];
    }
    return new UInt256(result);
  }

  /**
   * Return a bit-wise AND of this value and the supplied bytes.
   *
   * @param bytes the bytes to perform the operation with
   * @return the result of a bit-wise AND
   */
  public UInt256 and(Bytes32 bytes) {
    int[] result = new int[INTS_SIZE];
    for (int i = INTS_SIZE - 1, j = 28; i >= 0; --i, j -= 4) {
      int other = ((int) bytes.get(j) & 0xFF) << 24;
      other |= ((int) bytes.get(j + 1) & 0xFF) << 16;
      other |= ((int) bytes.get(i + 2) & 0xFF) << 8;
      other |= ((int) bytes.get(i + 3) & 0xFF);
      result[i] = this.ints[i] & other;
    }
    return new UInt256(result);
  }

  /**
   * Return a bit-wise OR of this value and the supplied value.
   *
   * @param value the value to perform the operation with
   * @return the result of a bit-wise OR
   */
  public UInt256 or(UInt256 value) {
    int[] result = new int[INTS_SIZE];
    for (int i = INTS_SIZE - 1; i >= 0; --i) {
      result[i] = this.ints[i] | value.ints[i];
    }
    return new UInt256(result);
  }

  /**
   * Return a bit-wise OR of this value and the supplied bytes.
   *
   * @param bytes the bytes to perform the operation with
   * @return the result of a bit-wise OR
   */
  public UInt256 or(Bytes32 bytes) {
    int[] result = new int[INTS_SIZE];
    for (int i = INTS_SIZE - 1, j = 28; i >= 0; --i, j -= 4) {
      result[i] = this.ints[i] | (((int) bytes.get(j) & 0xFF) << 24);
      result[i] |= ((int) bytes.get(j + 1) & 0xFF) << 16;
      result[i] |= ((int) bytes.get(j + 2) & 0xFF) << 8;
      result[i] |= ((int) bytes.get(j + 3) & 0xFF);
    }
    return new UInt256(result);
  }

  /**
   * Return a bit-wise NOT of this value.
   *
   * @return the result of a bit-wise NOT
   */
  public UInt256 not() {
    int[] result = new int[INTS_SIZE];
    for (int i = INTS_SIZE - 1; i >= 0; --i) {
      result[i] = ~(this.ints[i]);
    }
    return new UInt256(result);
  }

  @Override
  public boolean equals(Object object) {
    if (object == this) {
      return true;
    }
    if (!(object instanceof UInt256)) {
      return false;
    }
    UInt256 other = (UInt256) object;
    for (int i = 0; i < INTS_SIZE; ++i) {
      if (this.ints[i] != other.ints[i]) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    int result = 1;
    for (int i = 0; i < INTS_SIZE; ++i) {
      result = 31 * result + this.ints[i];
    }
    return result;
  }

  @Override
  public int compareTo(UInt256 other) {
    for (int i = 0; i < INTS_SIZE; ++i) {
      int cmp = Long.compare(((long) this.ints[i]) & LONG_MASK, ((long) other.ints[i]) & LONG_MASK);
      if (cmp != 0) {
        return cmp;
      }
    }
    return 0;
  }

  @Override
  public String toString() {
    return toBigInteger().toString();
  }

  @Override
  public BigInteger toBigInteger() {
    byte[] mag = new byte[32];
    for (int i = 0, j = 0; i < INTS_SIZE; ++i) {
      mag[j++] = (byte) (this.ints[i] >>> 24);
      mag[j++] = (byte) ((this.ints[i] >>> 16) & 0xFF);
      mag[j++] = (byte) ((this.ints[i] >>> 8) & 0xFF);
      mag[j++] = (byte) (this.ints[i] & 0xFF);
    }
    return new BigInteger(1, mag);
  }

  @Override
  public Bytes32 toBytes() {
    return Bytes32
        .wrap(
            new byte[] {
                (byte) (ints[0] >> 24),
                (byte) (ints[0] >> 16),
                (byte) (ints[0] >> 8),
                (byte) (ints[0]),
                (byte) (ints[1] >> 24),
                (byte) (ints[1] >> 16),
                (byte) (ints[1] >> 8),
                (byte) (ints[1]),
                (byte) (ints[2] >> 24),
                (byte) (ints[2] >> 16),
                (byte) (ints[2] >> 8),
                (byte) (ints[2]),
                (byte) (ints[3] >> 24),
                (byte) (ints[3] >> 16),
                (byte) (ints[3] >> 8),
                (byte) (ints[3]),
                (byte) (ints[4] >> 24),
                (byte) (ints[4] >> 16),
                (byte) (ints[4] >> 8),
                (byte) (ints[4]),
                (byte) (ints[5] >> 24),
                (byte) (ints[5] >> 16),
                (byte) (ints[5] >> 8),
                (byte) (ints[5]),
                (byte) (ints[6] >> 24),
                (byte) (ints[6] >> 16),
                (byte) (ints[6] >> 8),
                (byte) (ints[6]),
                (byte) (ints[7] >> 24),
                (byte) (ints[7] >> 16),
                (byte) (ints[7] >> 8),
                (byte) (ints[7])});
  }
}
