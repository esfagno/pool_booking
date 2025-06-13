CREATE OR REPLACE FUNCTION decrement_remaining_bookings()
RETURNS TRIGGER AS $$
BEGIN

UPDATE user_subscription
SET remaining_bookings = remaining_bookings - 1
WHERE user_id = NEW.user_id
  AND remaining_bookings > 0
  AND subscription_id IN (
    SELECT id
    FROM subscription
    WHERE status = 'ACTIVE'
)
    RETURNING *;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_decrement_remaining_bookings
    AFTER INSERT ON booking
    FOR EACH ROW
    EXECUTE FUNCTION decrement_remaining_bookings();
