/**
 *  A module containing utility classes that sweeten the use of records.
 *
 * @see com.github.forax.recordutil.MapTrait
 * @see com.github.forax.recordutil.WithTrait
 * @see com.github.forax.recordutil.Wither
 */
module com.github.forax.recordutil {
  requires static com.fasterxml.jackson.core;

  exports com.github.forax.recordutil;
}
