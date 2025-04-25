using AsianMovieApi.Controllers;
using AsianMovieApi.Data;
using AsianMovieApi.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Xunit;

namespace AsianMovieApi.Tests
{
    public class MoviesControllerTests
    {
        [Fact]
        public async Task GetMovies_ReturnsAllMovies()
        {
            var db = TestHelper.GetDbContext("GetMoviesTest");
            db.Movies.AddRange(
                new Movie { Title = "Movie A", Genre = "Drama", ReleaseDate = DateTime.Now, ImageUrl = "https://example.com/a.jpg" },
                new Movie { Title = "Movie B", Genre = "Action", ReleaseDate = DateTime.Now, ImageUrl = "https://example.com/b.jpg" }
            );
            await db.SaveChangesAsync();

            var controller = new MoviesController(db);
            var result = await controller.GetMovies();

            var movies = Assert.IsType<List<Movie>>(result.Value);
            Assert.Equal(2, movies.Count);
        }

        [Fact]
        public async Task GetMovie_ReturnsCorrectMovie()
        {
            var db = TestHelper.GetDbContext("GetMovieByIdTest");
            var movie = new Movie
            {
                Title = "Test Movie",
                Genre = "Horror",
                ReleaseDate = DateTime.Now,
                ImageUrl = "https://example.com/test.jpg",
                Reviews = new List<Review>
                {
                    new Review { Rating = 8, ReviewerName = "Test", ReviewText = "Nice!" }
                }
            };
            db.Movies.Add(movie);
            await db.SaveChangesAsync();

            var controller = new MoviesController(db);
            var result = await controller.GetMovie(movie.Id);

            var value = Assert.IsType<Movie>(result.Value);
            Assert.Equal("Test Movie", value.Title);
        }

        [Fact]
        public async Task PostMovie_CreatesNewMovie()
        {
            var db = TestHelper.GetDbContext("PostMovieTest");
            var controller = new MoviesController(db);

            var newMovie = new Movie
            {
                Title = "New Movie",
                Genre = "Thriller",
                ReleaseDate = DateTime.Today,
                Rating = 8.3,
                ImageUrl = "https://example.com/poster.jpg"
            };

            var result = await controller.PostMovie(newMovie);
            var created = Assert.IsType<CreatedAtActionResult>(result.Result);
            var movie = Assert.IsType<Movie>(created.Value);

            Assert.Equal("New Movie", movie.Title);
        }

        [Fact]
        public async Task PostReview_AddsReviewToMovie()
        {
            var db = TestHelper.GetDbContext("PostReviewTest");

            var movie = new Movie
            {
                Title = "Review Me",
                Genre = "Action",
                ReleaseDate = DateTime.Now,
                ImageUrl = "https://example.com/reviewme.jpg"
            };
            db.Movies.Add(movie);
            await db.SaveChangesAsync();

            var controller = new MoviesController(db);

            var review = new Review
            {
                ReviewerName = "Jane",
                ReviewText = "Loved it!",
                Rating = 9
            };

            var result = await controller.PostReview(movie.Id, review);
            var okResult = Assert.IsType<OkObjectResult>(result.Result);
            var addedReview = Assert.IsType<Review>(okResult.Value);

            Assert.Equal("Jane", addedReview.ReviewerName);
            Assert.Equal(movie.Id, addedReview.MovieId);
        }

        [Fact]
        public async Task Search_ReturnsMatchingMovies()
        {
            var db = TestHelper.GetDbContext("SearchTest");
            db.Movies.AddRange(
                new Movie { Title = "Parasite", Genre = "Thriller", ReleaseDate = DateTime.Now, ImageUrl = "https://example.com/parasite.jpg" },
                new Movie { Title = "Train to Busan", Genre = "Horror", ReleaseDate = DateTime.Now, ImageUrl = "https://example.com/busan.jpg" }
            );
            await db.SaveChangesAsync();

            var controller = new MoviesController(db);
            var result = await controller.Search("busan", null);

            var list = Assert.IsType<List<Movie>>(result.Value);
            Assert.Single(list);
            Assert.Equal("Train to Busan", list[0].Title);
        }

        [Fact]
        public async Task GetAverageRating_ReturnsCorrectValue()
        {
            var db = TestHelper.GetDbContext("AvgRatingTest");

            var movie = new Movie
            {
                Title = "Average Test",
                Genre = "Drama",
                ReleaseDate = DateTime.Now,
                ImageUrl = "https://example.com/average.jpg",
                Reviews = new List<Review>
                {
                    new Review { Rating = 7, ReviewerName = "Alice", ReviewText = "Good" },
                    new Review { Rating = 9, ReviewerName = "Bob", ReviewText = "Great!" }
                }
            };

            db.Movies.Add(movie);
            await db.SaveChangesAsync();

            var controller = new MoviesController(db);
            var result = await controller.GetAverageRating(movie.Id);

            var avg = Assert.IsType<double>(result.Value);
            Assert.Equal(8.0, avg);
        }

        [Fact]
        public async Task GetTopMovies_ReturnsHighestRated()
        {
            var db = TestHelper.GetDbContext("TopRatedTest");

            db.Movies.AddRange(
                new Movie
                {
                    Title = "Low Rated",
                    Genre = "Drama",
                    ReleaseDate = DateTime.Now,
                    ImageUrl = "https://example.com/low.jpg",
                    Reviews = new List<Review>
                    {
                        new Review { Rating = 4, ReviewerName = "User", ReviewText = "Meh" }
                    }
                },
                new Movie
                {
                    Title = "High Rated",
                    Genre = "Action",
                    ReleaseDate = DateTime.Now,
                    ImageUrl = "https://example.com/high.jpg",
                    Reviews = new List<Review>
                    {
                        new Review { Rating = 10, ReviewerName = "Pro", ReviewText = "Amazing!" }
                    }
                }
            );
            await db.SaveChangesAsync();

            var controller = new MoviesController(db);
            var result = await controller.GetTopMovies(1);
            var list = Assert.IsType<List<Movie>>(result.Value);

            Assert.Single(list);
            Assert.Equal("High Rated", list[0].Title);
        }

        [Fact]
        public async Task GetMovies_SortedByTitleAsc_ReturnsInCorrectOrder()
        {
            var db = TestHelper.GetDbContext("SortTitleAsc");
            db.Movies.AddRange(
                new Movie { Title = "Z Movie", Genre = "Drama", ReleaseDate = DateTime.Now, ImageUrl = "https://x.com" },
                new Movie { Title = "A Movie", Genre = "Action", ReleaseDate = DateTime.Now, ImageUrl = "https://y.com" }
            );
            await db.SaveChangesAsync();

            var controller = new MoviesController(db);
            var result = await controller.GetMovies(sort: "title", order: "asc");

            var movies = Assert.IsType<List<Movie>>(result.Value);
            Assert.Equal("A Movie", movies[0].Title);
            Assert.Equal("Z Movie", movies[1].Title);
        }

        [Fact]
        public async Task GetMovies_Pagination_ReturnsCorrectPage()
        {
            var db = TestHelper.GetDbContext("PaginationTest");

            for (int i = 1; i <= 15; i++)
            {
                db.Movies.Add(new Movie
                {
                    Title = $"Movie {i}",
                    Genre = "Test",
                    ReleaseDate = DateTime.Now,
                    ImageUrl = $"https://example.com/{i}.jpg"
                });
            }

            await db.SaveChangesAsync();

            var controller = new MoviesController(db);
            var result = await controller.GetMovies(sort: "title", page: 2, pageSize: 5);

            var movies = Assert.IsType<List<Movie>>(result.Value);
            Assert.Equal(5, movies.Count);
        }

        [Fact]
        public async Task GetMovies_FilterByGenre_ReturnsOnlyMatchingGenre()
        {
            var db = TestHelper.GetDbContext("FilterGenre");
            db.Movies.AddRange(
                new Movie { Title = "A", Genre = "Horror", ReleaseDate = DateTime.Now, ImageUrl = "https://1.com" },
                new Movie { Title = "B", Genre = "Action", ReleaseDate = DateTime.Now, ImageUrl = "https://2.com" }
            );
            await db.SaveChangesAsync();

            var controller = new MoviesController(db);
            var result = await controller.GetMovies(genre: "Action");

            var movies = Assert.IsType<List<Movie>>(result.Value);
            Assert.Single(movies);
            Assert.Equal("Action", movies[0].Genre);
        }

        [Fact]
        public async Task GetMovies_FilterSortPaginate_ReturnsCorrectSubset()
        {
            var db = TestHelper.GetDbContext("ComboTest");
            for (int i = 1; i <= 20; i++)
            {
                db.Movies.Add(new Movie
                {
                    Title = $"Busan {i}",
                    Genre = "Horror",
                    ReleaseDate = DateTime.Now.AddDays(-i),
                    ImageUrl = "https://img.com"
                });
            }
            await db.SaveChangesAsync();

            var controller = new MoviesController(db);
            var result = await controller.GetMovies(title: "Busan", genre: "Horror", sort: "releasedate", order: "desc", page: 2, pageSize: 5);

            var movies = Assert.IsType<List<Movie>>(result.Value);
            Assert.Equal(5, movies.Count);
            Assert.StartsWith("Busan", movies[0].Title);
        }

        [Fact]
        public async Task GetRandomMovie_ReturnsOneMovie()
        {
            var db = TestHelper.GetDbContext("RandomMovieTest");

            db.Movies.AddRange(
                new Movie { Title = "Movie A", Genre = "Action", ReleaseDate = DateTime.Now, ImageUrl = "https://example.com/a.jpg" },
                new Movie { Title = "Movie B", Genre = "Horror", ReleaseDate = DateTime.Now, ImageUrl = "https://example.com/b.jpg" }
            );
            await db.SaveChangesAsync();

            var controller = new MoviesController(db);
            var result = await controller.GetRandomMovie();

            var movie = Assert.IsType<Movie>(result.Value);
            Assert.False(string.IsNullOrWhiteSpace(movie.Title));
        }

        [Fact]
        public async Task PutMovie_UpdatesMovieDetails()
        {
            var db = TestHelper.GetDbContext("PutMovieTest");
            var movie = new Movie { Title = "Old", Genre = "Drama", ReleaseDate = DateTime.Now, ImageUrl = "https://old.com" };
            db.Movies.Add(movie);
            await db.SaveChangesAsync();

            var controller = new MoviesController(db);
            movie.Title = "Updated";

            var result = await controller.PutMovie(movie.Id, movie);
            Assert.IsType<NoContentResult>(result);

            var updated = await db.Movies.FindAsync(movie.Id);
            Assert.Equal("Updated", updated.Title);
        }

        [Fact]
        public async Task DeleteMovie_RemovesMovie()
        {
            var db = TestHelper.GetDbContext("DeleteMovieTest");
            var movie = new Movie { Title = "To Delete", Genre = "Horror", ReleaseDate = DateTime.Now, ImageUrl = "https://del.com" };
            db.Movies.Add(movie);
            await db.SaveChangesAsync();

            var controller = new MoviesController(db);
            var result = await controller.DeleteMovie(movie.Id);

            Assert.IsType<NoContentResult>(result);
            Assert.False(await db.Movies.AnyAsync(m => m.Id == movie.Id));
        }

        [Fact]
        public async Task PutReview_UpdatesReviewText()
        {
            var db = TestHelper.GetDbContext("PutReviewTest");
            var movie = new Movie
            {
                Title = "Review Movie",
                Genre = "Action",
                ReleaseDate = DateTime.Now,
                ImageUrl = "https://reviewmovie.com",
                Reviews = new List<Review>
        {
            new Review { ReviewerName = "Jane", ReviewText = "Old Text", Rating = 7 }
        }
            };
            db.Movies.Add(movie);
            await db.SaveChangesAsync();

            var review = movie.Reviews.First();
            var controller = new MoviesController(db);

            review.ReviewText = "Updated Review!";
            var result = await controller.UpdateReview(movie.Id, review.Id, review);

            Assert.IsType<NoContentResult>(result);

            var updated = await db.Reviews.FindAsync(review.Id);
            Assert.Equal("Updated Review!", updated.ReviewText);
        }

        [Fact]
        public async Task DeleteReview_RemovesReviewFromMovie()
        {
            var db = TestHelper.GetDbContext("DeleteReviewTest");
            var movie = new Movie
            {
                Title = "Delete Me",
                Genre = "Action",
                ReleaseDate = DateTime.Now,
                ImageUrl = "https://deleteme.com",
                Reviews = new List<Review>
        {
            new Review { ReviewerName = "Alice", ReviewText = "To delete", Rating = 5 }
        }
            };
            db.Movies.Add(movie);
            await db.SaveChangesAsync();

            var controller = new MoviesController(db);
            var reviewId = movie.Reviews.First().Id;

            var result = await controller.DeleteReview(movie.Id, reviewId);

            Assert.IsType<NoContentResult>(result);
            Assert.False(await db.Reviews.AnyAsync(r => r.Id == reviewId));
        }

        [Fact]
        public async Task GetLatestMovies_ReturnsMoviesSortedByReleaseDateDesc()
        {
            var db = TestHelper.GetDbContext("LatestMoviesTest");

            db.Movies.AddRange(
                new Movie
                {
                    Title = "Older Movie",
                    Genre = "Drama",
                    ReleaseDate = new DateTime(2010, 1, 1),
                    ImageUrl = "https://example.com/old.jpg"
                },
                new Movie
                {
                    Title = "Newer Movie",
                    Genre = "Action",
                    ReleaseDate = new DateTime(2023, 12, 15),
                    ImageUrl = "https://example.com/new.jpg"
                }
            );

            await db.SaveChangesAsync();

            var controller = new MoviesController(db);
            var result = await controller.GetLatestMovies(2);

            var movies = Assert.IsType<List<Movie>>(result.Value);
            Assert.Equal(2, movies.Count);
            Assert.Equal("Newer Movie", movies[0].Title);
            Assert.Equal("Older Movie", movies[1].Title);
        }

    }
}
