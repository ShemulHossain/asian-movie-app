using AsianMovieApi.Models;
using Microsoft.EntityFrameworkCore;
using System.Collections.Generic;

namespace AsianMovieApi.Data
{
    public class ApplicationDbContext : DbContext
    {
        public ApplicationDbContext(DbContextOptions<ApplicationDbContext> options)
        : base(options) { }

        public DbSet<Movie> Movies { get; set; }
        public DbSet<Review> Reviews { get; set; }
    }
}
