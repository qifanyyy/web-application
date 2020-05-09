import java.util.Objects;

class Star {
    final String name;
    final Integer birthYear;

    public Star(String name, Integer birthYear) {
        assert name != null;

        this.name = name;
        this.birthYear = birthYear;
    }

    @Override
    public String toString() {
        return "Star{" +
                "name='" + name + '\'' +
                ", birthYear=" + birthYear +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Star star = (Star) o;
        return name.equals(star.name) &&
                Objects.equals(birthYear, star.birthYear);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, birthYear);
    }
}
