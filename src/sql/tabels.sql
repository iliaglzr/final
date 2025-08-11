create table users (
    id int primary key auto_increment,
    name nvarchar(100) not null ,
    email nvarchar(200) not null unique  ,
    password nvarchar(200) not null
);

CREATE TABLE emails (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        sender_id INT NOT NULL,
                        subject VARCHAR(255),
                        body TEXT,
                        sent_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (sender_id) REFERENCES users(id)
);

CREATE TABLE email_recipients (
                                  id INT AUTO_INCREMENT PRIMARY KEY,
                                  email_id INT NOT NULL,
                                  recipient_id INT NOT NULL,
                                  is_read BOOLEAN DEFAULT FALSE,
                                  FOREIGN KEY (email_id) REFERENCES emails(id),
                                  FOREIGN KEY (recipient_id) REFERENCES users(id)
);
ALTER TABLE email_recipients ADD column body nvarchar(255) ;
ALTER TABLE email_recipients ADD column subject nvarchar(255);
SELECT * from emails;


