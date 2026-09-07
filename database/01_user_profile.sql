-- Optional SQL Server migration for an existing database.
-- The project also has hibernate.hbm2ddl.auto=update enabled for development.
USE [webst2];
GO
IF COL_LENGTH('dbo.Users', 'Phone') IS NULL
    ALTER TABLE dbo.Users ADD Phone varchar(20) NULL;
GO
IF COL_LENGTH('dbo.Users', 'Images') IS NULL
    ALTER TABLE dbo.Users ADD Images varchar(255) NULL;
GO
