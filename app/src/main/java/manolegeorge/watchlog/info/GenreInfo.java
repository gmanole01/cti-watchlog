package manolegeorge.watchlog.info;

public class GenreInfo {

  private final int id;
  private final String name;

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
