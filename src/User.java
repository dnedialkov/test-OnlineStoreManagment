public abstract class User {
    int id;
    String username;
   // private String password;

    public User(int id,String username/*, String password*/) {
        this.id=id;
        this.username = username;
      //  this.password = password;
    }

//    public String getPassword() {
//        return password;
//    }

    public String getUsername() {
        return username;
    }

    public int getId() {
        return id;
    }

    public abstract UserType getUserType();
}
