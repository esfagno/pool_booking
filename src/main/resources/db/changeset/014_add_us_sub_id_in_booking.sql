ALTER TABLE booking
    ADD COLUMN user_subscription_id INTEGER;

COMMENT ON COLUMN booking.user_subscription_id IS 'Ссылка на подписку, использованную при бронировании. NULL означает, что бронирование было создано без подписки (по правилу "1 бронь без подписки")';

ALTER TABLE booking
    ADD CONSTRAINT fk_booking_user_subscription
        FOREIGN KEY (user_subscription_id)
            REFERENCES user_subscription(id)
            ON DELETE SET NULL;