CREATE TABLE user_app_downloads (
                                    user_id UUID NOT NULL,
                                    app_id UUID NOT NULL,
                                    PRIMARY KEY (user_id, app_id)
);
