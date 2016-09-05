set(hamcrest-core_version 1.3)

set(hamcrest-core_sources
  org/hamcrest/BaseDescription.java
  org/hamcrest/BaseMatcher.java
  org/hamcrest/Condition.java
  org/hamcrest/core/AllOf.java
  org/hamcrest/core/AnyOf.java
  org/hamcrest/core/CombinableMatcher.java
  org/hamcrest/core/DescribedAs.java
  org/hamcrest/core/Every.java
  org/hamcrest/core/IsAnything.java
  org/hamcrest/core/IsCollectionContaining.java
  org/hamcrest/core/IsEqual.java
  org/hamcrest/core/IsInstanceOf.java
  org/hamcrest/core/Is.java
  org/hamcrest/core/IsNot.java
  org/hamcrest/core/IsNull.java
  org/hamcrest/core/IsSame.java
  org/hamcrest/CoreMatchers.java
  org/hamcrest/core/ShortcutCombination.java
  org/hamcrest/core/StringContains.java
  org/hamcrest/core/StringEndsWith.java
  org/hamcrest/core/StringStartsWith.java
  org/hamcrest/core/SubstringMatcher.java
  org/hamcrest/CustomMatcher.java
  org/hamcrest/CustomTypeSafeMatcher.java
  org/hamcrest/Description.java
  org/hamcrest/DiagnosingMatcher.java
  org/hamcrest/Factory.java
  org/hamcrest/FeatureMatcher.java
  org/hamcrest/internal/ArrayIterator.java
  org/hamcrest/internal/ReflectiveTypeFinder.java
  org/hamcrest/internal/SelfDescribingValueIterator.java
  org/hamcrest/internal/SelfDescribingValue.java
  org/hamcrest/MatcherAssert.java
  org/hamcrest/Matcher.java
  org/hamcrest/SelfDescribing.java
  org/hamcrest/StringDescription.java
  org/hamcrest/TypeSafeDiagnosingMatcher.java
  org/hamcrest/TypeSafeMatcher.java
)

add_jar(hamcrest-core
  OUTPUT_NAME hamcrest-core-${hamcrest-core_version}
  SOURCES ${hamcrest-core_sources}
)
