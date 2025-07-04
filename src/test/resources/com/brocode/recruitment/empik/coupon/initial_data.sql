insert into empik_coupon.Coupon(id,
                                uuid,
                                locale,
                                creation_date,
                                max_use,
                                curr_use)
values (1, 'UUID-TEST-OK', 'US', current_date, 100, 0);
insert into empik_coupon.Coupon(id,
                                uuid,
                                locale,
                                creation_date,
                                max_use,
                                curr_use)
values (2, 'UUID-TEST-CONSUMED', 'US', current_date, 100, 100);