IF DB_ID(N'ProductStore') IS NULL CREATE DATABASE ProductStore;
GO
USE ProductStore;
GO
IF OBJECT_ID(N'dbo.Users',N'U') IS NULL CREATE TABLE Users(
 id BIGINT IDENTITY PRIMARY KEY, full_name NVARCHAR(100) NOT NULL,
 email NVARCHAR(254) NOT NULL UNIQUE, password_hash VARCHAR(100) NOT NULL,
 role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER' CHECK(role IN('CUSTOMER','ADMIN')),
 phone VARCHAR(20) NOT NULL DEFAULT '', address NVARCHAR(500) NOT NULL DEFAULT '',
 failed_attempts INT NOT NULL DEFAULT 0, locked_until DATETIME2 NULL,
 created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME());
IF OBJECT_ID(N'dbo.Categories',N'U') IS NULL CREATE TABLE Categories(
 id BIGINT IDENTITY PRIMARY KEY, name NVARCHAR(80) NOT NULL UNIQUE,
 slug VARCHAR(80) NOT NULL UNIQUE, icon NVARCHAR(12) NOT NULL DEFAULT N'▦');
IF OBJECT_ID(N'dbo.ProductSkuSequence',N'SO') IS NULL CREATE SEQUENCE dbo.ProductSkuSequence AS BIGINT START WITH 1 INCREMENT BY 1 NO CYCLE;
IF OBJECT_ID(N'dbo.Products',N'U') IS NULL CREATE TABLE Products(
 id BIGINT IDENTITY PRIMARY KEY,
 sku VARCHAR(32) COLLATE Latin1_General_100_CI_AS NOT NULL CONSTRAINT DF_Products_sku DEFAULT('SP'+CONVERT(VARCHAR(20),NEXT VALUE FOR dbo.ProductSkuSequence)),
 category_id BIGINT NOT NULL REFERENCES Categories(id), name NVARCHAR(160) NOT NULL,
 brand NVARCHAR(80) NOT NULL DEFAULT '', description NVARCHAR(3000) NOT NULL,
 price DECIMAL(18,2) NOT NULL CHECK(price>0), original_price DECIMAL(18,2) NOT NULL CHECK(original_price>=0),
 stock INT NOT NULL CHECK(stock>=0), image_url NVARCHAR(500) NOT NULL,
 active BIT NOT NULL DEFAULT 1, featured BIT NOT NULL DEFAULT 0,
 created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(), version ROWVERSION,
 source_url NVARCHAR(500) NOT NULL DEFAULT '', source_retailer NVARCHAR(100) NOT NULL DEFAULT '', price_checked_at DATE NULL,
 CONSTRAINT UQ_Products_sku UNIQUE(sku), CONSTRAINT CK_Products_sku CHECK(LEN(sku) BETWEEN 1 AND 32 AND DATALENGTH(sku)=LEN(sku) AND sku COLLATE Latin1_General_100_BIN2 NOT LIKE '%[^A-Z0-9_-]%'));
IF OBJECT_ID(N'dbo.CartItems',N'U') IS NULL CREATE TABLE CartItems(
 user_id BIGINT NOT NULL REFERENCES Users(id),product_id BIGINT NOT NULL REFERENCES Products(id),
 quantity INT NOT NULL CHECK(quantity BETWEEN 1 AND 99),PRIMARY KEY(user_id,product_id));
IF OBJECT_ID(N'dbo.Wishlists',N'U') IS NULL CREATE TABLE Wishlists(
 user_id BIGINT NOT NULL REFERENCES Users(id),product_id BIGINT NOT NULL REFERENCES Products(id),PRIMARY KEY(user_id,product_id));
IF OBJECT_ID(N'dbo.Orders',N'U') IS NULL CREATE TABLE Orders(
 id BIGINT IDENTITY PRIMARY KEY,user_id BIGINT NOT NULL REFERENCES Users(id),recipient NVARCHAR(100) NOT NULL,
 phone VARCHAR(20) NOT NULL,address NVARCHAR(500) NOT NULL,note NVARCHAR(1000) NOT NULL DEFAULT '',
 status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK(status IN('PENDING','CONFIRMED','SHIPPING','COMPLETED','CANCELLED')),
 payment_method VARCHAR(20) NOT NULL DEFAULT 'COD' CHECK(payment_method='COD'),subtotal DECIMAL(18,2) NOT NULL CHECK(subtotal>=0),
 shipping_fee DECIMAL(18,2) NOT NULL CHECK(shipping_fee>=0),total DECIMAL(18,2) NOT NULL CHECK(total>=0),
 checkout_token VARCHAR(36) NOT NULL UNIQUE,created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME());
IF OBJECT_ID(N'dbo.OrderItems',N'U') IS NULL CREATE TABLE OrderItems(
 id BIGINT IDENTITY PRIMARY KEY,order_id BIGINT NOT NULL REFERENCES Orders(id),product_id BIGINT NOT NULL REFERENCES Products(id),
 product_name NVARCHAR(160) NOT NULL,image_url NVARCHAR(500) NOT NULL,unit_price DECIMAL(18,2) NOT NULL CHECK(unit_price>0),
 quantity INT NOT NULL CHECK(quantity BETWEEN 1 AND 99));
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE name='IX_Products_Category' AND object_id=OBJECT_ID('Products')) CREATE INDEX IX_Products_Category ON Products(category_id,active);
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE name='IX_Orders_UserDate' AND object_id=OBJECT_ID('Orders')) CREATE INDEX IX_Orders_UserDate ON Orders(user_id,created_at DESC);
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE name='IX_Orders_StatusDate' AND object_id=OBJECT_ID('Orders')) CREATE INDEX IX_Orders_StatusDate ON Orders(status,created_at);
IF COL_LENGTH('Products','source_url') IS NULL ALTER TABLE Products ADD source_url NVARCHAR(500) NOT NULL DEFAULT '';
IF COL_LENGTH('Products','source_retailer') IS NULL ALTER TABLE Products ADD source_retailer NVARCHAR(100) NOT NULL DEFAULT '';
IF COL_LENGTH('Products','price_checked_at') IS NULL ALTER TABLE Products ADD price_checked_at DATE NULL;
GO