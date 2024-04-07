public class Admin extends User {
    public Admin(int id,String username/*, String password*/) {
        super(id,username/*, password*/);
    }
    @Override
    public UserType getUserType() {
        return UserType.ADMIN;
    }

    @Override
    public String toString() {
        return "Admin{" + username + '}';
    }
}
