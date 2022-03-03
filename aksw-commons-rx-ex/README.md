
# Extended RxJava Operators

Tho content of this module are plain extensions of rx with no dependencies on further libraries (except for slfj4 logging).

## Operators for aggregation / accumulation

* `RxOps.takeGroups(long count, Function<I, K> itemToGroupKey)`: A filter operation that only accepts items related to `count` distinct group keys
* `RxOps.skipGroups(long count, Function<I, K> itemToGroupKey)`: A filter opertation that skips the first set of items that are related to `count` distinct group keys
* `RxOps.sequentialGroupBy(...)`: An operator that delegates consecutive items that map to the same group key to a custom accumulator. The accumulator may e.g. count items or collect them in a list.

