import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

class Star {
    @NotNull final String name;
    @Nullable final Integer birthYear;

    Star(@NotNull String name, @Nullable Integer birthYear) {
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
