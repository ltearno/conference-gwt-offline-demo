create table orders (id INTEGER PRIMARY KEY, date TEXT, addressCode TEXT, update_date TEXT);
create table order_items (id INTEGER PRIMARY KEY, order_id INTEGER, article_id INTEGER, quantity INTEGER, unit_price INTEGER, amount INTEGER, update_date TEXT);
create table articles (id INTEGER PRIMARY KEY, code TEXT, name TEXT, price INTEGER, picture TEXT, pdf TEXT, update_date TEXT);