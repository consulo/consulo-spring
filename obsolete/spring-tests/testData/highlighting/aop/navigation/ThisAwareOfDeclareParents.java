package foo.bar.aop;

interface MyMixin {}

class MyMixinImpl implements MyMixin {}


class Target {
    void foo() {}
}

class Impl implements MyMixin {
    void foo() {}
}

class Subclass extends Target {
    void foo() {}
}

class NotMatched {
    void foo() {}
}