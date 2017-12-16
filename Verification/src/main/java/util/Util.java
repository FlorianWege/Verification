package util;

public class Util {
    public static class Wrap<T> {
        private T _val;

        public T get() {
            return _val;
        }

        public void set(T val) {
            _val = val;
        }

        public Wrap(T val) {
            _val = val;
        }
    }
}
