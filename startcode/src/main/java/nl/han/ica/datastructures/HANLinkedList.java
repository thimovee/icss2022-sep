package nl.han.ica.datastructures;

public class HANLinkedList<T> implements IHANLinkedList<T> {

    private final Node<T> header = new Node<>(null, null);
    private int size = 0;

    @Override
    public void addFirst(T value) {
        header.next = new Node<>(value, header.next);
        size++;
    }

    @Override
    public void clear() {
        header.next = null;
        size = 0;
    }

    @Override
    public void insert(int index, T value) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException(index);
        }
        Node<T> previous = nodeBefore(index);
        previous.next = new Node<>(value, previous.next);
        size++;
    }

    @Override
    public void delete(int pos) {
        checkPosition(pos);
        Node<T> previous = nodeBefore(pos);
        previous.next = previous.next.next;
        size--;
    }

    @Override
    public T get(int pos) {
        checkPosition(pos);
        return nodeBefore(pos).next.value;
    }

    @Override
    public void removeFirst() {
        delete(0);
    }

    @Override
    public T getFirst() {
        return get(0);
    }

    @Override
    public int getSize() {
        return size;
    }

    private Node<T> nodeBefore(int index) {
        Node<T> current = header;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current;
    }

    private void checkPosition(int pos) {
        if (pos < 0 || pos >= size) {
            throw new IndexOutOfBoundsException(pos);
        }
    }

    private static class Node<T> {
        private final T value;
        private Node<T> next;

        private Node(T value, Node<T> next) {
            this.value = value;
            this.next = next;
        }
    }
}
