using AsianMovieApi.Data;
using AsianMovieApi.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace AsianMovieApi.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class MoviesController : ControllerBase
    {
        private readonly ApplicationDbContext _context;

        public MoviesController(ApplicationDbContext context)
        {
            _context = context;
        }

        // GET: api/movies/1
        [HttpGet("{id}")]
        public async Task<ActionResult<Movie>> GetMovie(int id)
        {
            var movie = await _context.Movies
                .Include(m => m.Reviews)
                .FirstOrDefaultAsync(m => m.Id == id);

            if (movie == null)
                return NotFound();

            return movie;
        }

        // GET: api/movies/search?title=busan
        [HttpGet("search")]
        public async Task<ActionResult<IEnumerable<Movie>>> Search(string? title, string? genre)
        {
            var query = _context.Movies.Include(m => m.Reviews).AsQueryable();

            if (!string.IsNullOrWhiteSpace(title))
                query = query.Where(m => m.Title.ToLower().Contains(title.ToLower()));

            if (!string.IsNullOrWhiteSpace(genre))
                query = query.Where(m => m.Genre.ToLower() == genre.ToLower());

            return await query.ToListAsync();
        }

        // GET: api/movies/{id}/average-rating
        [HttpGet("{id}/average-rating")]
        public async Task<ActionResult<double>> GetAverageRating(int id)
        {
            var movie = await _context.Movies.Include(m => m.Reviews).FirstOrDefaultAsync(m => m.Id == id);
            if (movie == null || movie.Reviews.Count == 0)
                return NotFound();

            return movie.Reviews.Average(r => r.Rating);
        }

        // GET: api/movies/top?count=5
        [HttpGet("top")]
        public async Task<ActionResult<IEnumerable<Movie>>> GetTopMovies(int count = 5)
        {
            return await _context.Movies
                .Include(m => m.Reviews)
                .OrderByDescending(m => m.Reviews.Any() ? m.Reviews.Average(r => r.Rating) : 0)
                .Take(count)
                .ToListAsync();
        }

        // GET: api/movies/latest?count=5
        [HttpGet("latest")]
        public async Task<ActionResult<IEnumerable<Movie>>> GetLatestMovies(int count = 5)
        {
            return await _context.Movies
                .OrderByDescending(m => m.ReleaseDate)
                .Take(count)
                .ToListAsync();
        }

        // GET: api/movies/random
        [HttpGet("random")]
        public async Task<ActionResult<Movie>> GetRandomMovie()
        {
            var count = await _context.Movies.CountAsync();
            if (count == 0)
                return NotFound();

            var index = new Random().Next(count);
            var movie = await _context.Movies.Skip(index).Include(m => m.Reviews).FirstOrDefaultAsync();

            return movie;
        }

        // GET: api/movies
        // Returns a paginated, sortable, and filterable list of movies.
        // Supports optional query parameters: title, genre, sort, order, page, pageSize.
        [HttpGet]
        public async Task<ActionResult<IEnumerable<Movie>>> GetMovies(
            string? genre = null,
            string? title = null,
            string? sort = "title",
            string? order = "asc",
            int page = 1,
            int pageSize = 10)
        {
            var query = _context.Movies.Include(m => m.Reviews).AsQueryable();

            // Filtering
            if (!string.IsNullOrWhiteSpace(genre))
                query = query.Where(m => m.Genre.ToLower() == genre.ToLower());

            if (!string.IsNullOrWhiteSpace(title))
                query = query.Where(m => m.Title.ToLower().Contains(title.ToLower()));

            // Sorting
            switch (sort?.ToLower())
            {
                case "title":
                    query = order == "desc" ? query.OrderByDescending(m => m.Title) : query.OrderBy(m => m.Title);
                    break;
                case "rating":
                    query = order == "desc"
                        ? query.OrderByDescending(m => m.Reviews.Any() ? m.Reviews.Average(r => r.Rating) : 0)
                        : query.OrderBy(m => m.Reviews.Any() ? m.Reviews.Average(r => r.Rating) : 0);
                    break;
                case "releasedate":
                    query = order == "desc" ? query.OrderByDescending(m => m.ReleaseDate) : query.OrderBy(m => m.ReleaseDate);
                    break;
            }

            // Pagination
            var skip = (page - 1) * pageSize;
            query = query.Skip(skip).Take(pageSize);

            return await query.ToListAsync();
        }


        // POST: api/movies
        [HttpPost]
        public async Task<ActionResult<Movie>> PostMovie(Movie movie)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            _context.Movies.Add(movie);
            await _context.SaveChangesAsync();

            return CreatedAtAction(nameof(GetMovie), new { id = movie.Id }, movie);
        }

        // POST: api/movies/1/reviews
        [HttpPost("{id}/reviews")]
        public async Task<ActionResult<Review>> PostReview(int id, Review review)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            var movie = await _context.Movies.FindAsync(id);
            if (movie == null)
                return NotFound();

            review.MovieId = id;
            _context.Reviews.Add(review);
            await _context.SaveChangesAsync();

            return Ok(review);
        }

        // PUT: api/movies/1
        [HttpPut("{id}")]
        public async Task<IActionResult> PutMovie(int id, Movie updatedMovie)
        {
            if (id != updatedMovie.Id)
                return BadRequest();

            _context.Entry(updatedMovie).State = EntityState.Modified;

            try
            {
                await _context.SaveChangesAsync();
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!_context.Movies.Any(m => m.Id == id))
                    return NotFound();
                else
                    throw;
            }

            return NoContent();
        }

        // PUT: api/movies/{movieId}/reviews/{reviewId}
        // Updates an existing review for a specific movie.
        [HttpPut("{movieId}/reviews/{reviewId}")]
        public async Task<IActionResult> UpdateReview(int movieId, int reviewId, Review updatedReview)
        {
            if (reviewId != updatedReview.Id || movieId != updatedReview.MovieId)
                return BadRequest("Mismatched IDs");

            var review = await _context.Reviews.FindAsync(reviewId);
            if (review == null)
                return NotFound();

            review.ReviewerName = updatedReview.ReviewerName;
            review.ReviewText = updatedReview.ReviewText;
            review.Rating = updatedReview.Rating;

            await _context.SaveChangesAsync();
            return NoContent();
        }

        // DELETE: api/movies/1
        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteMovie(int id)
        {
            var movie = await _context.Movies.FindAsync(id);
            if (movie == null)
                return NotFound();

            _context.Movies.Remove(movie);
            await _context.SaveChangesAsync();

            return NoContent();
        }

        // DELETE: api/movies/{movieId}/reviews/{reviewId}
        // Deletes a specific review from a movie.
        [HttpDelete("{movieId}/reviews/{reviewId}")]
        public async Task<IActionResult> DeleteReview(int movieId, int reviewId)
        {
            var review = await _context.Reviews.FirstOrDefaultAsync(r => r.Id == reviewId && r.MovieId == movieId);
            if (review == null)
                return NotFound();

            _context.Reviews.Remove(review);
            await _context.SaveChangesAsync();

            return NoContent();
        }

    }
}
