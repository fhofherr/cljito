(ns cljito.core-test
  (:import [java.util ArrayList List]
           [org.mockito Mockito])
  (:use midje.sweet
        cljito.core))

(fact "mocks are Mockito mocks"
  (.isMock (Mockito/mockingDetails (mock List))) => true)

(fact "spies are Mockito spy"
  (-> (Mockito/mockingDetails (spy (ArrayList.))) (.isSpy))
  => true)

(fact "mocks are stubbed, just like Java's"
  (.get (when-> (mock List)
                (.get 0)
                (.thenReturn "it works"))
        0)
  => "it works"

  (.get (when-> (mock List)
                (.get 0)
                (.thenThrow (classes RuntimeException)))
        0)
  => (throws RuntimeException))

(facts "verify support"
  (against-background
    (around :checks
            (let [cleared-once (mock List)
                  never-cleared (mock List)]
              (.clear cleared-once)
              ?form)))

  (verify-> never-cleared (.clear))
  => (throws AssertionError)

  (verify-> never-cleared never (.clear))
  =not=> (throws AssertionError)

  (verify-> cleared-once (.clear))
  =not=> (throws AssertionError)

  (verify-> cleared-once 1 (.clear))
  =not=> (throws AssertionError)

  (verify-> cleared-once 2 (.clear))
  => (throws AssertionError))

(facts "argument matchers support"
  (.get (when-> (mock List)
                (.get (any-int))
                (.thenReturn "argument matchers works"))
        12345)
  => "argument matchers works")

(facts "support for do* stubbings"
  (.get (do-return "it works"
                   (.when (mock List))
                   (.get 0))
        0)
  => "it works"

  (.clear (do-throw (UnsupportedOperationException.)
             (.when (mock List))
             (.clear)))
  => (throws UnsupportedOperationException))
