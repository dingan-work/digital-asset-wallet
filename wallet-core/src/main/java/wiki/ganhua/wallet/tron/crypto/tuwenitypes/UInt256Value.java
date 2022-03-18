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

/**
 * Represents a 256-bit (32 bytes) unsigned integer value.
 *
 * <p>
 * A {@link UInt256Value} is an unsigned integer value stored with 32 bytes, so whose value can range between 0 and
 * 2^256-1.
 *
 * <p>
 * This interface defines operations for value types with a 256-bit precision range. The methods provided by this
 * interface take parameters of the same type (and also {@code long}. This provides type safety by ensuring calculations
 * cannot mix different {@code UInt256Value} types.
 *
 * <p>
 * Where only a pure numerical 256-bit value is required, {@link UInt256} should be used.
 *
 * <p>
 * It is strongly advised to extend {@link BaseUInt256Value} rather than implementing this interface directly. Doing so
 * provides type safety in that quantities of different units cannot be mixed accidentally.
 *
 * @param <T> The concrete type of the value.
 */
public interface UInt256Value<T extends UInt256Value<T>> extends Comparable<T> {

  /**
   * Returns true is the value is 0.
   *
   * @return True if this is the value 0.
   */
  default boolean isZero() {
    return toBytes().isZero();
  }

  /**
   * Returns a value that is {@code (this + value)}.
   *
   * @param value The amount to be added to this value.
   * @return {@code this + value}
   */
  T add(T value);

  /**
   * Returns a value that is {@code (this + value)}.
   *
   * @param value The amount to be added to this value.
   * @return {@code this + value}
   */
  T add(long value);

  /**
   * Provides the value as a BigInteger.
   *
   * @return This value as a {@link BigInteger}.
   */
  default BigInteger toBigInteger() {
    return toBytes().toUnsignedBigInteger();
  }

  /**
   * Provides the value as bytes.
   *
   * @return The value as bytes.
   */
  Bytes32 toBytes();
}
