package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationExceptions;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;


import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Film findFilm(Long id) {
        return filmStorage.findFilm(id);
    }

    public List<Film> findAllFilms() {
        return filmStorage.findAllFilms();
    }

    public Film createFilm(Film newFilm) {
        filmValidation(newFilm);
        return filmStorage.createFilm(newFilm);
    }

    public Film updateFilm(Film newFilm) {
        filmValidation(newFilm);
        return filmStorage.updateFilm(newFilm);
    }

    public Film setLikeToMovie(Long id, Long userId) {
        userStorage.findUser(userId);
        Film films = findFilm(id);
        films.getLikes().add(userId);
        return films;
    }

    public Film removeLikeFromMovie(Long id, Long userId) {
        userStorage.findUser(userId);
        Film film = findFilm(id);
        film.getLikes().remove(userId);
        return film;
    }

    public List<Film> getPopularFilms(int count) {
        return findAllFilms().stream()
                .filter(movie -> movie.getLikes() != null)
                .sorted((o1, o2) -> Integer.compare(o2.getLikes().size(), o1.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }

    private void filmValidation(Film newFilm) {
        if (newFilm == null) {
            log.error("Пользователь попытался создать новый фильм с пустым объектом");
            throw new ValidationExceptions("Необходимо заполнить все поля");
        }
        if (newFilm.getName().isEmpty()) {
            log.error("Пользователь попытался создать новый фильм с пустым названием");
            throw new ValidationExceptions("Название не должно быть пустым");
        }
        if (newFilm.getDescription().isEmpty() || newFilm.getDescription().length() > 200) {
            log.error("Пользователь попытался создать новый фильм с пустым описанием или длинной более 200 символов");
            throw new ValidationExceptions("Введите описание должной не более 200 символов");
        }
        if (newFilm.getReleaseDate().isBefore(LocalDate.of(1895,12,28))) {
            log.error("Пользователь попытался создать новый фильм с датой релиза ранее 28.12.1895 года");
            throw new ValidationExceptions("Дата выxода не может быть раньше 28.12.1895 года");
        }
        if (newFilm.getDuration() <= 0) {
            log.error("Пользователь попытался создать новый фильм с длительностью меньше 1 минуты");
            throw new ValidationExceptions("Длительность не может быть меньше 1 минуты");
        }
    }
}
