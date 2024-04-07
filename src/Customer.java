public class Customer extends User {
    public Customer(int id,String username/*, String password*/) {
        super(id,username/*, password*/);
    }
    @Override
    public UserType getUserType() {
        return UserType.CLIENT;
    }

    @Override
    public String toString() {
        return "Client{" + username + '}';
    }
}
