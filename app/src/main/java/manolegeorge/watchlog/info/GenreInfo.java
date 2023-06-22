package manolegeorge.watchlog.info;

public class GenreInfo {

  private int id;
  private String name;

  public GenreInfo(int mId, String mName) {
    this.id = mId;
    this.name = mName;
  }

  public int getId() {
    return this.id;
  }

  public String getName() {
    return this.name;
  }

}
