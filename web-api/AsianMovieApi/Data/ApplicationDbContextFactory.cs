using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Design;
using Microsoft.Extensions.Configuration;
using System.IO;

namespace AsianMovieApi.Data
{
    public class ApplicationDbContextFactory : IDesignTimeDbContextFactory<ApplicationDbContext>
    {
        public ApplicationDbContext CreateDbContext(string[] args)
        {
            IConfigurationRoot configuration = new ConfigurationBuilder()
                .SetBasePath(Directory.GetCurrentDirectory())
                .AddJsonFile("appsettings.json")
                .AddEnvironmentVariables()
                .Build();

            var conn = configuration.GetConnectionString("DefaultConnection");

            // Use env var if available
            var password = Environment.GetEnvironmentVariable("SQL_DB_PASSWORD");
            if (!string.IsNullOrWhiteSpace(password))
            {
                conn = conn.Replace("__SQL_DB_PASSWORD__", password);
            }
            var optionsBuilder = new DbContextOptionsBuilder<ApplicationDbContext>();
            optionsBuilder.UseSqlServer(conn, options =>
            {
                options.EnableRetryOnFailure();
            });

            return new ApplicationDbContext(optionsBuilder.Options);
        }
    }

}
