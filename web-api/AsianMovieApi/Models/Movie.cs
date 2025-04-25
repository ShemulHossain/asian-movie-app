using Microsoft.AspNetCore.Mvc.ViewEngines;
using System.ComponentModel.DataAnnotations;

namespace AsianMovieApi.Models
{
    public class Movie
    {
        public int Id { get; set; }

        [Required]
        [StringLength(100)]
        public string Title { get; set; }

        [Required]
        [StringLength(50)]
        public string Genre { get; set; }

        [Required]
        public DateTime ReleaseDate { get; set; }

        [Range(0, 10)]
        public double Rating { get; set; }

        [Url]
        public string ImageUrl { get; set; }

        public string? Description { get; set; }

        public ICollection<Review>? Reviews { get; set; }
    }
}
