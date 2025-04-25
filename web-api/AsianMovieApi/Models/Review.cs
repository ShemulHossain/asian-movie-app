using System.ComponentModel.DataAnnotations;
using System.Text.Json.Serialization;

namespace AsianMovieApi.Models
{
    public class Review
    {
        public int Id { get; set; }

        public int MovieId { get; set; }

        [Required]
        [StringLength(50)]
        public string ReviewerName { get; set; }

        [Required]
        [StringLength(500)]
        public string ReviewText { get; set; }

        [Range(0, 10)]
        public double Rating { get; set; }

        [JsonIgnore]
        public Movie? Movie { get; set; }
    }
}
