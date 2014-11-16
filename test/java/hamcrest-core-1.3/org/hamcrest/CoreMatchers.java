// Generated source.
package org.hamcrest;

public class CoreMatchers {

  /**
   * Creates a matcher that matches if the examined object matches <b>ALL</b> of the specified matchers.
   * <p/>
   * For example:
   * <pre>assertThat("myValue", allOf(startsWith("my"), containsString("Val")))</pre>
   */
  public static <T> org.hamcrest.Matcher<T> allOf(java.lang.Iterable<org.hamcrest.Matcher<? super T>> matchers) {
    return org.hamcrest.core.AllOf.<T>allOf(matchers);
  }

  /**
   * Creates a matcher that matches if the examined object matches <b>ALL</b> of the specified matchers.
   * <p/>
   * For example:
   * <pre>assertThat("myValue", allOf(startsWith("my"), containsString("Val")))</pre>
   */
  public static <T> org.hamcrest.Matcher<T> allOf(org.hamcrest.Matcher<? super T>... matchers) {
    return org.hamcrest.core.AllOf.<T>allOf(matchers);
  }

  /**
   * Creates a matcher that matches if the examined object matches <b>ALL</b> of the specified matchers.
   * <p/>
   * For example:
   * <pre>assertThat("myValue", allOf(startsWith("my"), containsString("Val")))</pre>
   */
  public static <T> org.hamcrest.Matcher<T> allOf(org.hamcrest.Matcher<? super T> first, org.hamcrest.Matcher<? super T> second) {
    return org.hamcrest.core.AllOf.<T>allOf(first, second);
  }

  /**
   * Creates a matcher that matches if the examined object matches <b>ALL</b> of the specified matchers.
   * <p/>
   * For example:
   * <pre>assertThat("myValue", allOf(startsWith("my"), containsString("Val")))</pre>
   */
  public static <T> org.hamcrest.Matcher<T> allOf(org.hamcrest.Matcher<? super T> first, org.hamcrest.Matcher<? super T> second, org.hamcrest.Matcher<? super T> third) {
    return org.hamcrest.core.AllOf.<T>allOf(first, second, third);
  }

  /**
   * Creates a matcher that matches if the examined object matches <b>ALL</b> of the specified matchers.
   * <p/>
   * For example:
   * <pre>assertThat("myValue", allOf(startsWith("my"), containsString("Val")))</pre>
   */
  public static <T> org.hamcrest.Matcher<T> allOf(org.hamcrest.Matcher<? super T> first, org.hamcrest.Matcher<? super T> second, org.hamcrest.Matcher<? super T> third, org.hamcrest.Matcher<? super T> fourth) {
    return org.hamcrest.core.AllOf.<T>allOf(first, second, third, fourth);
  }

  /**
   * Creates a matcher that matches if the examined object matches <b>ALL</b> of the specified matchers.
   * <p/>
   * For example:
   * <pre>assertThat("myValue", allOf(startsWith("my"), containsString("Val")))</pre>
   */
  public static <T> org.hamcrest.Matcher<T> allOf(org.hamcrest.Matcher<? super T> first, org.hamcrest.Matcher<? super T> second, org.hamcrest.Matcher<? super T> third, org.hamcrest.Matcher<? super T> fourth, org.hamcrest.Matcher<? super T> fifth) {
    return org.hamcrest.core.AllOf.<T>allOf(first, second, third, fourth, fifth);
  }

  /**
   * Creates a matcher that matches if the examined object matches <b>ALL</b> of the specified matchers.
   * <p/>
   * For example:
   * <pre>assertThat("myValue", allOf(startsWith("my"), containsString("Val")))</pre>
   */
  public static <T> org.hamcrest.Matcher<T> allOf(org.hamcrest.Matcher<? super T> first, org.hamcrest.Matcher<? super T> second, org.hamcrest.Matcher<? super T> third, org.hamcrest.Matcher<? super T> fourth, org.hamcrest.Matcher<? super T> fifth, org.hamcrest.Matcher<? super T> sixth) {
    return org.hamcrest.core.AllOf.<T>allOf(first, second, third, fourth, fifth, sixth);
  }

  /**
   * Creates a matcher that matches if the examined object matches <b>ANY</b> of the specified matchers.
   * <p/>
   * For example:
   * <pre>assertThat("myValue", anyOf(startsWith("foo"), containsString("Val")))</pre>
   */
  public static <T> org.hamcrest.core.AnyOf<T> anyOf(java.lang.Iterable<org.hamcrest.Matcher<? super T>> matchers) {
    return org.hamcrest.core.AnyOf.<T>anyOf(matchers);
  }

  /**
   * Creates a matcher that matches if the examined object matches <b>ANY</b> of the specified matchers.
   * <p/>
   * For example:
   * <pre>assertThat("myValue", anyOf(startsWith("foo"), containsString("Val")))</pre>
   */
  public static <T> org.hamcrest.core.AnyOf<T> anyOf(org.hamcrest.Matcher<T> first, org.hamcrest.Matcher<? super T> second, org.hamcrest.Matcher<? super T> third) {
    return org.hamcrest.core.AnyOf.<T>anyOf(first, second, third);
  }

  /**
   * Creates a matcher that matches if the examined object matches <b>ANY</b> of the specified matchers.
   * <p/>
   * For example:
   * <pre>assertThat("myValue", anyOf(startsWith("foo"), containsString("Val")))</pre>
   */
  public static <T> org.hamcrest.core.AnyOf<T> anyOf(org.hamcrest.Matcher<T> first, org.hamcrest.Matcher<? super T> second, org.hamcrest.Matcher<? super T> third, org.hamcrest.Matcher<? super T> fourth) {
    return org.hamcrest.core.AnyOf.<T>anyOf(first, second, third, fourth);
  }

  /**
   * Creates a matcher that matches if the examined object matches <b>ANY</b> of the specified matchers.
   * <p/>
   * For example:
   * <pre>assertThat("myValue", anyOf(startsWith("foo"), containsString("Val")))</pre>
   */
  public static <T> org.hamcrest.core.AnyOf<T> anyOf(org.hamcrest.Matcher<T> first, org.hamcrest.Matcher<? super T> second, org.hamcrest.Matcher<? super T> third, org.hamcrest.Matcher<? super T> fourth, org.hamcrest.Matcher<? super T> fifth) {
    return org.hamcrest.core.AnyOf.<T>anyOf(first, second, third, fourth, fifth);
  }

  /**
   * Creates a matcher that matches if the examined object matches <b>ANY</b> of the specified matchers.
   * <p/>
   * For example:
   * <pre>assertThat("myValue", anyOf(startsWith("foo"), containsString("Val")))</pre>
   */
  public static <T> org.hamcrest.core.AnyOf<T> anyOf(org.hamcrest.Matcher<T> first, org.hamcrest.Matcher<? super T> second, org.hamcrest.Matcher<? super T> third, org.hamcrest.Matcher<? super T> fourth, org.hamcrest.Matcher<? super T> fifth, org.hamcrest.Matcher<? super T> sixth) {
    return org.hamcrest.core.AnyOf.<T>anyOf(first, second, third, fourth, fifth, sixth);
  }

  /**
   * Creates a matcher that matches if the examined object matches <b>ANY</b> of the specified matchers.
   * <p/>
   * For example:
   * <pre>assertThat("myValue", anyOf(startsWith("foo"), containsString("Val")))</pre>
   */
  public static <T> org.hamcrest.core.AnyOf<T> anyOf(org.hamcrest.Matcher<T> first, org.hamcrest.Matcher<? super T> second) {
    return org.hamcrest.core.AnyOf.<T>anyOf(first, second);
  }

  /**
   * Creates a matcher that matches if the examined object matches <b>ANY</b> of the specified matchers.
   * <p/>
   * For example:
   * <pre>assertThat("myValue", anyOf(startsWith("foo"), containsString("Val")))</pre>
   */
  public static <T> org.hamcrest.core.AnyOf<T> anyOf(org.hamcrest.Matcher<? super T>... matchers) {
    return org.hamcrest.core.AnyOf.<T>anyOf(matchers);
  }

  /**
   * Creates a matcher that matches when both of the specified matchers match the examined object.
   * <p/>
   * For example:
   * <pre>assertThat("fab", both(containsString("a")).and(containsString("b")))</pre>
   */
  public static <LHS> org.hamcrest.core.CombinableMatcher.CombinableBothMatcher<LHS> both(org.hamcrest.Matcher<? super LHS> matcher) {
    return org.hamcrest.core.CombinableMatcher.<LHS>both(matcher);
  }

  /**
   * Creates a matcher that matches when either of the specified matchers match the examined object.
   * <p/>
   * For example:
   * <pre>assertThat("fan", either(containsString("a")).and(containsString("b")))</pre>
   */
  public static <LHS> org.hamcrest.core.CombinableMatcher.CombinableEitherMatcher<LHS> either(org.hamcrest.Matcher<? super LHS> matcher) {
    return org.hamcrest.core.CombinableMatcher.<LHS>either(matcher);
  }

  /**
   * Wraps an existing matcher, overriding its description with that specified.  All other functions are
   * delegated to the decorated matcher, including its mismatch description.
   * <p/>
   * For example:
   * <pre>describedAs("a big decimal equal to %0", equalTo(myBigDecimal), myBigDecimal.toPlainString())</pre>
   * 
   * @param description
   *     the new description for the wrapped matcher
   * @param matcher
   *     the matcher to wrap
   * @param values
   *     optional values to insert into the tokenised description
   */
  public static <T> org.hamcrest.Matcher<T> describedAs(java.lang.String description, org.hamcrest.Matcher<T> matcher, java.lang.Object... values) {
    return org.hamcrest.core.DescribedAs.<T>describedAs(description, matcher, values);
  }

  /**
   * Creates a matcher for {@link Iterable}s that only matches when a single pass over the
   * examined {@link Iterable} yields items that are all matched by the specified
   * <code>itemMatcher</code>.
   * <p/>
   * For example:
   * <pre>assertThat(Arrays.asList("bar", "baz"), everyItem(startsWith("ba")))</pre>
   * 
   * @param itemMatcher
   *     the matcher to apply to every item provided by the examined {@link Iterable}
   */
  public static <U> org.hamcrest.Matcher<java.lang.Iterable<U>> everyItem(org.hamcrest.Matcher<U> itemMatcher) {
    return org.hamcrest.core.Every.<U>everyItem(itemMatcher);
  }

  /**
   * A shortcut to the frequently used <code>is(equalTo(x))</code>.
   * <p/>
   * For example:
   * <pre>assertThat(cheese, is(smelly))</pre>
   * instead of:
   * <pre>assertThat(cheese, is(equalTo(smelly)))</pre>
   */
  public static <T> org.hamcrest.Matcher<T> is(T value) {
    return org.hamcrest.core.Is.<T>is(value);
  }

  /**
   * Decorates another Matcher, retaining its behaviour, but allowing tests
   * to be slightly more expressive.
   * <p/>
   * For example:
   * <pre>assertThat(cheese, is(equalTo(smelly)))</pre>
   * instead of:
   * <pre>assertThat(cheese, equalTo(smelly))</pre>
   */
  public static <T> org.hamcrest.Matcher<T> is(org.hamcrest.Matcher<T> matcher) {
    return org.hamcrest.core.Is.<T>is(matcher);
  }

  /**
   * A shortcut to the frequently used <code>is(instanceOf(SomeClass.class))</code>.
   * <p/>
   * For example:
   * <pre>assertThat(cheese, is(Cheddar.class))</pre>
   * instead of:
   * <pre>assertThat(cheese, is(instanceOf(Cheddar.class)))</pre>
   * 
   * @deprecated use isA(Class<T> type) instead.
   */
  public static <T> org.hamcrest.Matcher<T> is(java.lang.Class<T> type) {
    return org.hamcrest.core.Is.<T>is(type);
  }

  /**
   * A shortcut to the frequently used <code>is(instanceOf(SomeClass.class))</code>.
   * <p/>
   * For example:
   * <pre>assertThat(cheese, isA(Cheddar.class))</pre>
   * instead of:
   * <pre>assertThat(cheese, is(instanceOf(Cheddar.class)))</pre>
   */
  public static <T> org.hamcrest.Matcher<T> isA(java.lang.Class<T> type) {
    return org.hamcrest.core.Is.<T>isA(type);
  }

  /**
   * Creates a matcher that always matches, regardless of the examined object.
   */
  public static org.hamcrest.Matcher<java.lang.Object> anything() {
    return org.hamcrest.core.IsAnything.anything();
  }

  /**
   * Creates a matcher that always matches, regardless of the examined object, but describes
   * itself with the specified {@link String}.
   * 
   * @param description
   *     a meaningful {@link String} used when describing itself
   */
  public static org.hamcrest.Matcher<java.lang.Object> anything(java.lang.String description) {
    return org.hamcrest.core.IsAnything.anything(description);
  }

  /**
   * Creates a matcher for {@link Iterable}s that only matches when a single pass over the
   * examined {@link Iterable} yields at least one item that is equal to the specified
   * <code>item</code>.  Whilst matching, the traversal of the examined {@link Iterable}
   * will stop as soon as a matching item is found.
   * <p/>
   * For example:
   * <pre>assertThat(Arrays.asList("foo", "bar"), hasItem("bar"))</pre>
   * 
   * @param item
   *     the item to compare against the items provided by the examined {@link Iterable}
   */
  public static <T> org.hamcrest.Matcher<java.lang.Iterable<? super T>> hasItem(T item) {
    return org.hamcrest.core.IsCollectionContaining.<T>hasItem(item);
  }

  /**
   * Creates a matcher for {@link Iterable}s that only matches when a single pass over the
   * examined {@link Iterable} yields at least one item that is matched by the specified
   * <code>itemMatcher</code>.  Whilst matching, the traversal of the examined {@link Iterable}
   * will stop as soon as a matching item is found.
   * <p/>
   * For example:
   * <pre>assertThat(Arrays.asList("foo", "bar"), hasItem(startsWith("ba")))</pre>
   * 
   * @param itemMatcher
   *     the matcher to apply to items provided by the examined {@link Iterable}
   */
  public static <T> org.hamcrest.Matcher<java.lang.Iterable<? super T>> hasItem(org.hamcrest.Matcher<? super T> itemMatcher) {
    return org.hamcrest.core.IsCollectionContaining.<T>hasItem(itemMatcher);
  }

  /**
   * Creates a matcher for {@link Iterable}s that matches when consecutive passes over the
   * examined {@link Iterable} yield at least one item that is equal to the corresponding
   * item from the specified <code>items</code>.  Whilst matching, each traversal of the
   * examined {@link Iterable} will stop as soon as a matching item is found.
   * <p/>
   * For example:
   * <pre>assertThat(Arrays.asList("foo", "bar", "baz"), hasItems("baz", "foo"))</pre>
   * 
   * @param items
   *     the items to compare against the items provided by the examined {@link Iterable}
   */
  public static <T> org.hamcrest.Matcher<java.lang.Iterable<T>> hasItems(T... items) {
    return org.hamcrest.core.IsCollectionContaining.<T>hasItems(items);
  }

  /**
   * Creates a matcher for {@link Iterable}s that matches when consecutive passes over the
   * examined {@link Iterable} yield at least one item that is matched by the corresponding
   * matcher from the specified <code>itemMatchers</code>.  Whilst matching, each traversal of
   * the examined {@link Iterable} will stop as soon as a matching item is found.
   * <p/>
   * For example:
   * <pre>assertThat(Arrays.asList("foo", "bar", "baz"), hasItems(endsWith("z"), endsWith("o")))</pre>
   * 
   * @param itemMatchers
   *     the matchers to apply to items provided by the examined {@link Iterable}
   */
  public static <T> org.hamcrest.Matcher<java.lang.Iterable<T>> hasItems(org.hamcrest.Matcher<? super T>... itemMatchers) {
    return org.hamcrest.core.IsCollectionContaining.<T>hasItems(itemMatchers);
  }

  /**
   * Creates a matcher that matches when the examined object is logically equal to the specified
   * <code>operand</code>, as determined by calling the {@link java.lang.Object#equals} method on
   * the <b>examined</b> object.
   * 
   * <p>If the specified operand is <code>null</code> then the created matcher will only match if
   * the examined object's <code>equals</code> method returns <code>true</code> when passed a
   * <code>null</code> (which would be a violation of the <code>equals</code> contract), unless the
   * examined object itself is <code>null</code>, in which case the matcher will return a positive
   * match.</p>
   * 
   * <p>The created matcher provides a special behaviour when examining <code>Array</code>s, whereby
   * it will match if both the operand and the examined object are arrays of the same length and
   * contain items that are equal to each other (according to the above rules) <b>in the same
   * indexes</b>.</p> 
   * <p/>
   * For example:
   * <pre>
   * assertThat("foo", equalTo("foo"));
   * assertThat(new String[] {"foo", "bar"}, equalTo(new String[] {"foo", "bar"}));
   * </pre>
   */
  public static <T> org.hamcrest.Matcher<T> equalTo(T operand) {
    return org.hamcrest.core.IsEqual.<T>equalTo(operand);
  }

  /**
   * Creates a matcher that matches when the examined object is an instance of the specified <code>type</code>,
   * as determined by calling the {@link java.lang.Class#isInstance(Object)} method on that type, passing the
   * the examined object.
   * 
   * <p>The created matcher forces a relationship between specified type and the examined object, and should be
   * used when it is necessary to make generics conform, for example in the JMock clause
   * <code>with(any(Thing.class))</code></p>
   * <p/>
   * For example: 
   * <pre>assertThat(new Canoe(), instanceOf(Canoe.class));</pre>
   */
  public static <T> org.hamcrest.Matcher<T> any(java.lang.Class<T> type) {
    return org.hamcrest.core.IsInstanceOf.<T>any(type);
  }

  /**
   * Creates a matcher that matches when the examined object is an instance of the specified <code>type</code>,
   * as determined by calling the {@link java.lang.Class#isInstance(Object)} method on that type, passing the
   * the examined object.
   * 
   * <p>The created matcher assumes no relationship between specified type and the examined object.</p>
   * <p/>
   * For example: 
   * <pre>assertThat(new Canoe(), instanceOf(Paddlable.class));</pre>
   */
  public static <T> org.hamcrest.Matcher<T> instanceOf(java.lang.Class<?> type) {
    return org.hamcrest.core.IsInstanceOf.<T>instanceOf(type);
  }

  /**
   * Creates a matcher that wraps an existing matcher, but inverts the logic by which
   * it will match.
   * <p/>
   * For example:
   * <pre>assertThat(cheese, is(not(equalTo(smelly))))</pre>
   * 
   * @param matcher
   *     the matcher whose sense should be inverted
   */
  public static <T> org.hamcrest.Matcher<T> not(org.hamcrest.Matcher<T> matcher) {
    return org.hamcrest.core.IsNot.<T>not(matcher);
  }

  /**
   * A shortcut to the frequently used <code>not(equalTo(x))</code>.
   * <p/>
   * For example:
   * <pre>assertThat(cheese, is(not(smelly)))</pre>
   * instead of:
   * <pre>assertThat(cheese, is(not(equalTo(smelly))))</pre>
   * 
   * @param value
   *     the value that any examined object should <b>not</b> equal
   */
  public static <T> org.hamcrest.Matcher<T> not(T value) {
    return org.hamcrest.core.IsNot.<T>not(value);
  }

  /**
   * Creates a matcher that matches if examined object is <code>null</code>.
   * <p/>
   * For example:
   * <pre>assertThat(cheese, is(nullValue())</pre>
   */
  public static org.hamcrest.Matcher<java.lang.Object> nullValue() {
    return org.hamcrest.core.IsNull.nullValue();
  }

  /**
   * Creates a matcher that matches if examined object is <code>null</code>. Accepts a
   * single dummy argument to facilitate type inference.
   * <p/>
   * For example:
   * <pre>assertThat(cheese, is(nullValue(Cheese.class))</pre>
   * 
   * @param type
   *     dummy parameter used to infer the generic type of the returned matcher
   */
  public static <T> org.hamcrest.Matcher<T> nullValue(java.lang.Class<T> type) {
    return org.hamcrest.core.IsNull.<T>nullValue(type);
  }

  /**
   * A shortcut to the frequently used <code>not(nullValue())</code>.
   * <p/>
   * For example:
   * <pre>assertThat(cheese, is(notNullValue()))</pre>
   * instead of:
   * <pre>assertThat(cheese, is(not(nullValue())))</pre>
   */
  public static org.hamcrest.Matcher<java.lang.Object> notNullValue() {
    return org.hamcrest.core.IsNull.notNullValue();
  }

  /**
   * A shortcut to the frequently used <code>not(nullValue(X.class)). Accepts a
   * single dummy argument to facilitate type inference.</code>.
   * <p/>
   * For example:
   * <pre>assertThat(cheese, is(notNullValue(X.class)))</pre>
   * instead of:
   * <pre>assertThat(cheese, is(not(nullValue(X.class))))</pre>
   * 
   * @param type
   *     dummy parameter used to infer the generic type of the returned matcher
   */
  public static <T> org.hamcrest.Matcher<T> notNullValue(java.lang.Class<T> type) {
    return org.hamcrest.core.IsNull.<T>notNullValue(type);
  }

  /**
   * Creates a matcher that matches only when the examined object is the same instance as
   * the specified target object.
   * 
   * @param target
   *     the target instance against which others should be assessed
   */
  public static <T> org.hamcrest.Matcher<T> sameInstance(T target) {
    return org.hamcrest.core.IsSame.<T>sameInstance(target);
  }

  /**
   * Creates a matcher that matches only when the examined object is the same instance as
   * the specified target object.
   * 
   * @param target
   *     the target instance against which others should be assessed
   */
  public static <T> org.hamcrest.Matcher<T> theInstance(T target) {
    return org.hamcrest.core.IsSame.<T>theInstance(target);
  }

  /**
   * Creates a matcher that matches if the examined {@link String} contains the specified
   * {@link String} anywhere.
   * <p/>
   * For example:
   * <pre>assertThat("myStringOfNote", containsString("ring"))</pre>
   * 
   * @param substring
   *     the substring that the returned matcher will expect to find within any examined string
   */
  public static org.hamcrest.Matcher<java.lang.String> containsString(java.lang.String substring) {
    return org.hamcrest.core.StringContains.containsString(substring);
  }

  /**
   * Creates a matcher that matches if the examined {@link String} starts with the specified
   * {@link String}.
   * <p/>
   * For example:
   * <pre>assertThat("myStringOfNote", startsWith("my"))</pre>
   * 
   * @param prefix
   *      the substring that the returned matcher will expect at the start of any examined string
   */
  public static org.hamcrest.Matcher<java.lang.String> startsWith(java.lang.String prefix) {
    return org.hamcrest.core.StringStartsWith.startsWith(prefix);
  }

  /**
   * Creates a matcher that matches if the examined {@link String} ends with the specified
   * {@link String}.
   * <p/>
   * For example:
   * <pre>assertThat("myStringOfNote", endsWith("Note"))</pre>
   * 
   * @param suffix
   *      the substring that the returned matcher will expect at the end of any examined string
   */
  public static org.hamcrest.Matcher<java.lang.String> endsWith(java.lang.String suffix) {
    return org.hamcrest.core.StringEndsWith.endsWith(suffix);
  }

}
