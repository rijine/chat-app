package chatappserver;

public class LinkedList
{
    private class Node
    {
        Node next;
        ServerThread data;
        long id;

        public Node(ServerThread d, long i) {
            next = null;
            data = d;
            id = i;
        }

        public Node(ServerThread d, long i, Node n)
        {
            next = n;
            data = d;
            id = i;
        }

        public ServerThread getServerThread() {
            return data;
        }

        public long getID() {
            return id;
        }

        public void setServerThread(ServerThread d) {
            data = d;
        }

        public void setID(long i) {
            id = i;
        }

        public Node getNext() {
            return next;
        }

        public void setNext(Node n) {
            next = n;
        }
    }
    
    private Node head;
    private int listCount;
    public LinkedList() {
            head = new Node(null, 0);
            listCount = 0;
    }

    public void add(ServerThread data, long id) {
        Node temp = new Node(data, id);
        Node current = head;
        while(current.getNext() != null)  {
                current = current.getNext();
        }
        current.setNext(temp);
        listCount++;
    }

    public void add(ServerThread data, long id, int index) {
        Node temp = new Node(data, id);
        Node current = head;
        for(int i = 1; i < index && current.getNext() != null; i++) {
                current = current.getNext();
        }
        temp.setNext(current.getNext());
        current.setNext(temp);
        listCount++;
    }

    public ServerThread find(long id) {
        Node current = head;
        int i = 0;
        //while(current.getNext() != null) {
        while(i <= listCount) {
            if (current.getID() == id)
                return current.getServerThread();
            else {
                if (current.getNext() != null)
                    current = current.getNext();
            }
            i++;
        }
        return null;
    }

    public boolean remove(long id) {
        Node current = head;
        while(current.getNext() != null) {
            if (current.getID() == id) {
                current.setNext(current.getNext().getNext());
                listCount--;
                return true;
            }
            else {
                current = current.getNext();
            }
        }
        return false;
    }
}