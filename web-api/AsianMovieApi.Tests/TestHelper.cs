using AsianMovieApi.Data;
using Microsoft.EntityFrameworkCore;

namespace AsianMovieApi.Tests
{
    public static class TestHelper
    {
        public static ApplicationDbContext GetDbContext(string dbName)
        {
            var options = new DbContextOptionsBuilder<ApplicationDbContext>()
                .UseInMemoryDatabase(databaseName: dbName)
                .Options;

            var dbContext = new ApplicationDbContext(options);
            dbContext.Database.EnsureCreated();

            return dbContext;
        }
    }
}
