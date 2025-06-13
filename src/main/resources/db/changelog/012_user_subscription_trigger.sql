CREATE OR REPLACE FUNCTION check_subscription_active()
RETURNS TRIGGER AS $$
BEGIN
  IF EXISTS (
    SELECT 1
    FROM subscription
    WHERE id = NEW.subscription_id
      AND status IN ('EXPIRED', 'CANCELLED')
  ) THEN
    RAISE EXCEPTION 'Нельзя добавить неактивную подписку (EXPIRED или CANCELLED) пользователю';
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_check_subscription_active
    BEFORE INSERT ON user_subscription
    FOR EACH ROW
    EXECUTE FUNCTION check_subscription_active();
