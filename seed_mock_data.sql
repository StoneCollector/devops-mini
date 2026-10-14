-- =========================================================================
-- Smart Catering Services - Comprehensive Realistic Seed Data Script
-- Populates diverse Vendors, Cuisines, Resources, Customers, Slots & Bookings
-- =========================================================================

USE cbms_db;

-- 1. CLEAN UP PREVIOUS MOCK TEST DATA (Preserve base seed accounts if any)
DELETE FROM catering_events WHERE customer_id IN (SELECT id FROM users WHERE email LIKE '%@smartcatering.com' AND email NOT IN ('admin@smartcatering.com', 'chef.mario@smartcatering.com', 'urban.spice@smartcatering.com', 'morning.brunch@smartcatering.com'))
   OR customer_id IN (SELECT id FROM users WHERE email IN ('vikram.mehta@tcs.com', 'ananya.sen@infosys.com', 'rohit.verma@gmail.com', 'sneha.kapoor@outlook.com', 'amit.sharma@nexuslabs.in', 'neha.gupta@deloitte.com', 'kavita.nair@gmail.com', 'rahul.bhatia@zomato.com'));

DELETE FROM booking WHERE user_id IN (SELECT id FROM users WHERE email IN ('vikram.mehta@tcs.com', 'ananya.sen@infosys.com', 'rohit.verma@gmail.com', 'sneha.kapoor@outlook.com', 'amit.sharma@nexuslabs.in', 'neha.gupta@deloitte.com', 'kavita.nair@gmail.com', 'rahul.bhatia@zomato.com'));

DELETE FROM vendor_cuisines WHERE vendor_id IN (SELECT id FROM users WHERE email IN ('royal.feasts@smartcatering.com', 'coastal.curries@smartcatering.com', 'wok.artisan@smartcatering.com', 'swaminarayan.catering@smartcatering.com', 'bella.italia@smartcatering.com', 'royal.mithai@smartcatering.com'));

DELETE FROM vendor_resource_pools WHERE vendor_id IN (SELECT id FROM users WHERE email IN ('royal.feasts@smartcatering.com', 'coastal.curries@smartcatering.com', 'wok.artisan@smartcatering.com', 'swaminarayan.catering@smartcatering.com', 'bella.italia@smartcatering.com', 'royal.mithai@smartcatering.com'));

DELETE FROM vendor_profiles WHERE vendor_id IN (SELECT id FROM users WHERE email IN ('royal.feasts@smartcatering.com', 'coastal.curries@smartcatering.com', 'wok.artisan@smartcatering.com', 'swaminarayan.catering@smartcatering.com', 'bella.italia@smartcatering.com', 'royal.mithai@smartcatering.com'));

DELETE FROM users WHERE email IN ('royal.feasts@smartcatering.com', 'coastal.curries@smartcatering.com', 'wok.artisan@smartcatering.com', 'swaminarayan.catering@smartcatering.com', 'bella.italia@smartcatering.com', 'royal.mithai@smartcatering.com',
                                  'vikram.mehta@tcs.com', 'ananya.sen@infosys.com', 'rohit.verma@gmail.com', 'sneha.kapoor@outlook.com', 'amit.sharma@nexuslabs.in', 'neha.gupta@deloitte.com', 'kavita.nair@gmail.com', 'rahul.bhatia@zomato.com');

-- 2. INSERT VENDORS (6 Diverse Top-Tier Catering Businesses)
INSERT INTO users (name, email, password, phone_number, address_line, city, pincode, organization_name, gst_number, role, active) VALUES
('Rajesh Sharma', 'royal.feasts@smartcatering.com', 'Vendor@123', '9820112233', 'S-12, Rajputana Enclave, Linking Road, Bandra West', 'Mumbai', '400050', 'Royal Rajputana Feasts LLP', '27AABCR1234F1Z1', 'VENDOR', 1),
('Priya Naik', 'coastal.curries@smartcatering.com', 'Vendor@123', '9820223344', 'Shop 4, Fisherman Wharf Lane, Mahim West', 'Mumbai', '400016', 'Malabar Coastal Hospitality', '27AABCM2345G1Z2', 'VENDOR', 1),
('David Chen', 'wok.artisan@smartcatering.com', 'Vendor@123', '9820334455', 'Unit 8, Silicon Food Park, Hiranandani, Powai', 'Mumbai', '400076', 'Asian Wok Culinary Co.', '27AABCA3456H1Z3', 'VENDOR', 1),
('Hiren Patel', 'swaminarayan.catering@smartcatering.com', 'Vendor@123', '9820445566', 'Bhakti Bhavan Complex, R.B. Mehta Marg, Ghatkopar East', 'Mumbai', '400077', 'Shree Swaminarayan Rasoi Ltd', '27AABCS4567J1Z4', 'VENDOR', 1),
('Marco Valenti', 'bella.italia@smartcatering.com', 'Vendor@123', '9820556677', '7 Colaba Causeway, Opp Gateway Pier', 'Mumbai', '400005', 'Bella Cucina Gourmet Caterers', '27AABCB5678K1Z5', 'VENDOR', 1),
('Anita Deshmukh', 'royal.mithai@smartcatering.com', 'Vendor@123', '9820667788', 'Plaza 3, Veer Savarkar Marg, Dadar West', 'Mumbai', '400028', 'Royal Mithai & Confectionery', '27AABCP6789L1Z6', 'VENDOR', 1);

-- 3. INSERT CUSTOMERS (8 Diverse Retail & Corporate Customers)
INSERT INTO users (name, email, password, phone_number, address_line, city, pincode, organization_name, gst_number, role, active) VALUES
('Vikram Mehta', 'vikram.mehta@tcs.com', 'Customer@123', '9819001122', 'TCS Yantra Park, Subhash Nagar, Thane West', 'Mumbai', '400606', 'Tata Consultancy Services Ltd', '27AAACT2727Q1ZW', 'CUSTOMER', 1),
('Ananya Sen', 'ananya.sen@infosys.com', 'Customer@123', '9819112233', 'Infosys Tower, SEZ Campus, Mindspace, Airoli', 'Navi Mumbai', '400708', 'Infosys Limited', '27AAACI4444P1ZX', 'CUSTOMER', 1),
('Rohit Verma', 'rohit.verma@gmail.com', 'Customer@123', '9819223344', 'Flat 402, Sea Crest Towers, Worli Sea Face', 'Mumbai', '400018', NULL, NULL, 'CUSTOMER', 1),
('Sneha Kapoor', 'sneha.kapoor@outlook.com', 'Customer@123', '9819334455', 'B-14 Palm Springs, Juhu Tara Road, Juhu', 'Mumbai', '400049', NULL, NULL, 'CUSTOMER', 1),
('Amit Sharma', 'amit.sharma@nexuslabs.in', 'Customer@123', '9819445566', 'Floor 5, WeWork Enam Sambhav, BKC, Bandra East', 'Mumbai', '400051', 'Nexus Labs Tech India', '27AABCN9988M1ZY', 'CUSTOMER', 1),
('Neha Gupta', 'neha.gupta@deloitte.com', 'Customer@123', '9819556677', 'Deloitte Center, Tower B, Senapati Bapat Marg, Lower Parel', 'Mumbai', '400013', 'Deloitte Shared Services India', '27AAACD1122K1Z9', 'CUSTOMER', 1),
('Kavita Nair', 'kavita.nair@gmail.com', 'Customer@123', '9819667788', '12 Rosewood Villa, Deonar Farm Road, Chembur', 'Mumbai', '400088', NULL, NULL, 'CUSTOMER', 1),
('Rahul Bhatia', 'rahul.bhatia@zomato.com', 'Customer@123', '9819778899', 'One International Center, Elphinstone Road', 'Mumbai', '400013', 'Zomato Operations Hub', '27AABCZ3344Q1ZZ', 'CUSTOMER', 1);

-- 4. INSERT VENDOR PROFILES
INSERT INTO vendor_profiles (vendor_id, business_name, business_phone, business_address, service_radius, gstin, fssai_license_no, operational_days, advance_booking_days, opening_time, closing_time, takeaway_enabled, ready_to_dine_enabled, ready_to_dine_start_time, ready_to_dine_end_time)
SELECT id, 'Royal Rajputana Feasts & Banquets', '9820112233', 'S-12, Rajputana Enclave, Linking Road, Bandra West, Mumbai', 45, '27AABCR1234F1Z1', '11520018000451', 'MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY,SUNDAY', 2, '08:00:00', '23:59:00', 1, 1, '11:00:00', '23:00:00' FROM users WHERE email = 'royal.feasts@smartcatering.com';

INSERT INTO vendor_profiles (vendor_id, business_name, business_phone, business_address, service_radius, gstin, fssai_license_no, operational_days, advance_booking_days, opening_time, closing_time, takeaway_enabled, ready_to_dine_enabled, ready_to_dine_start_time, ready_to_dine_end_time)
SELECT id, 'Malabar & Konkan Coastal Kitchen', '9820223344', 'Shop 4, Fisherman Wharf Lane, Mahim West, Mumbai', 35, '27AABCM2345G1Z2', '11521025000982', 'MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY,SUNDAY', 1, '09:00:00', '23:30:00', 1, 1, '11:30:00', '22:30:00' FROM users WHERE email = 'coastal.curries@smartcatering.com';

INSERT INTO vendor_profiles (vendor_id, business_name, business_phone, business_address, service_radius, gstin, fssai_license_no, operational_days, advance_booking_days, opening_time, closing_time, takeaway_enabled, ready_to_dine_enabled, ready_to_dine_start_time, ready_to_dine_end_time)
SELECT id, 'The Asian Wok & Dim Sum Studio', '9820334455', 'Unit 8, Silicon Food Park, Hiranandani, Powai, Mumbai', 25, '27AABCA3456H1Z3', '11522031000124', 'MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY,SUNDAY', 0, '10:30:00', '23:59:00', 1, 0, NULL, NULL FROM users WHERE email = 'wok.artisan@smartcatering.com';

INSERT INTO vendor_profiles (vendor_id, business_name, business_phone, business_address, service_radius, gstin, fssai_license_no, operational_days, advance_booking_days, opening_time, closing_time, takeaway_enabled, ready_to_dine_enabled, ready_to_dine_start_time, ready_to_dine_end_time)
SELECT id, 'Shree Swaminarayan Pure Veg & Jain Caterers', '9820445566', 'Bhakti Bhavan Complex, R.B. Mehta Marg, Ghatkopar East, Mumbai', 50, '27AABCS4567J1Z4', '11519012000876', 'MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY,SUNDAY', 2, '08:00:00', '23:00:00', 1, 1, '11:00:00', '22:00:00' FROM users WHERE email = 'swaminarayan.catering@smartcatering.com';

INSERT INTO vendor_profiles (vendor_id, business_name, business_phone, business_address, service_radius, gstin, fssai_license_no, operational_days, advance_booking_days, opening_time, closing_time, takeaway_enabled, ready_to_dine_enabled, ready_to_dine_start_time, ready_to_dine_end_time)
SELECT id, 'Bella Cucina Artisanal Italian Catering', '9820556677', '7 Colaba Causeway, Opp Gateway Pier, Colaba, Mumbai', 35, '27AABCB5678K1Z5', '11523009000331', 'MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY,SUNDAY', 1, '11:30:00', '23:45:00', 1, 1, '12:00:00', '23:00:00' FROM users WHERE email = 'bella.italia@smartcatering.com';

INSERT INTO vendor_profiles (vendor_id, business_name, business_phone, business_address, service_radius, gstin, fssai_license_no, operational_days, advance_booking_days, opening_time, closing_time, takeaway_enabled, ready_to_dine_enabled, ready_to_dine_start_time, ready_to_dine_end_time)
SELECT id, 'Pâtisserie & Royal Mithai Confectioners', '9820667788', 'Plaza 3, Veer Savarkar Marg, Dadar West, Mumbai', 40, '27AABCP6789L1Z6', '11521015000554', 'MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY,SUNDAY', 0, '08:30:00', '23:00:00', 1, 0, NULL, NULL FROM users WHERE email = 'royal.mithai@smartcatering.com';

-- 5. INSERT VENDOR RESOURCE POOLS
INSERT INTO vendor_resource_pools (vendor_id, staff_count, utensil_sets, delivery_vehicles, venue_cover_capacity)
SELECT id, 24, 320, 5, 180 FROM users WHERE email = 'royal.feasts@smartcatering.com';

INSERT INTO vendor_resource_pools (vendor_id, staff_count, utensil_sets, delivery_vehicles, venue_cover_capacity)
SELECT id, 18, 220, 4, 100 FROM users WHERE email = 'coastal.curries@smartcatering.com';

INSERT INTO vendor_resource_pools (vendor_id, staff_count, utensil_sets, delivery_vehicles, venue_cover_capacity)
SELECT id, 14, 160, 3, 0 FROM users WHERE email = 'wok.artisan@smartcatering.com';

INSERT INTO vendor_resource_pools (vendor_id, staff_count, utensil_sets, delivery_vehicles, venue_cover_capacity)
SELECT id, 35, 500, 7, 250 FROM users WHERE email = 'swaminarayan.catering@smartcatering.com';

INSERT INTO vendor_resource_pools (vendor_id, staff_count, utensil_sets, delivery_vehicles, venue_cover_capacity)
SELECT id, 16, 180, 3, 80 FROM users WHERE email = 'bella.italia@smartcatering.com';

INSERT INTO vendor_resource_pools (vendor_id, staff_count, utensil_sets, delivery_vehicles, venue_cover_capacity)
SELECT id, 12, 140, 3, 0 FROM users WHERE email = 'royal.mithai@smartcatering.com';

-- 6. INSERT VENDOR CUISINES (18 Diverse Signature Offerings)
-- Royal Rajputana
INSERT INTO vendor_cuisines (vendor_id, cuisine_name, description, price_per_head, dietary_flags, service_styles, min_guests, max_guests, active)
SELECT id, 'Royal Rajasthani Banquet', 'Dal Baati Churma, Gatte ki Sabzi, Ker Sangri, Shahi Pulao and Malpua Rabdi', 850.00, 'VEGETARIAN', 'BUFFET,LIVE_STATION', 30, 600, 1 FROM users WHERE email = 'royal.feasts@smartcatering.com';
INSERT INTO vendor_cuisines (vendor_id, cuisine_name, description, price_per_head, dietary_flags, service_styles, min_guests, max_guests, active)
SELECT id, 'Shahi Rajputana Kebab & Biryani Feast', 'Laal Maas, Dum Biryani, Galouti Kebabs, Roomali Roti, and Phirni', 980.00, 'NON_VEG,HALAL', 'BUFFET,PLATED', 25, 450, 1 FROM users WHERE email = 'royal.feasts@smartcatering.com';
INSERT INTO vendor_cuisines (vendor_id, cuisine_name, description, price_per_head, dietary_flags, service_styles, min_guests, max_guests, active)
SELECT id, 'Marwari Business Lunch Box', 'Gatta Curry, Paneer Lababdar, Missi Roti, Jeera Pulao & Gulab Jamun', 420.00, 'VEGETARIAN', 'PLATED,FAMILY_STYLE', 10, 150, 1 FROM users WHERE email = 'royal.feasts@smartcatering.com';

-- Coastal Curries
INSERT INTO vendor_cuisines (vendor_id, cuisine_name, description, price_per_head, dietary_flags, service_styles, min_guests, max_guests, active)
SELECT id, 'Mangalorean Seafood Platter', 'Surmai Tawa Fry, Prawns Ghee Roast, Kori Rotti, Neer Dosa & Sol Kadhi', 890.00, 'NON_VEG', 'BUFFET,PLATED', 15, 250, 1 FROM users WHERE email = 'coastal.curries@smartcatering.com';
INSERT INTO vendor_cuisines (vendor_id, cuisine_name, description, price_per_head, dietary_flags, service_styles, min_guests, max_guests, active)
SELECT id, 'Kerala Traditional Sadya', '24-item authentic Onam style sadya served with red rice, avial, sambar, payasam', 480.00, 'VEGETARIAN,JAIN', 'PLATED,FAMILY_STYLE', 20, 400, 1 FROM users WHERE email = 'coastal.curries@smartcatering.com';
INSERT INTO vendor_cuisines (vendor_id, cuisine_name, description, price_per_head, dietary_flags, service_styles, min_guests, max_guests, active)
SELECT id, 'Malabar Dum Biryani Box', 'Tender chicken dum biryani with raita, pickle, and Sulaimani chai shot', 380.00, 'HALAL,NON_VEG', 'PLATED', 10, 200, 1 FROM users WHERE email = 'coastal.curries@smartcatering.com';

-- Asian Wok Studio
INSERT INTO vendor_cuisines (vendor_id, cuisine_name, description, price_per_head, dietary_flags, service_styles, min_guests, max_guests, active)
SELECT id, 'Live Pan-Asian Wok & Teppanyaki', 'Customizable noodles, wok vegetables, chili garlic prawns, kung pao chicken & momos', 750.00, 'NON_VEG,VEGETARIAN', 'LIVE_STATION,BUFFET', 20, 300, 1 FROM users WHERE email = 'wok.artisan@smartcatering.com';
INSERT INTO vendor_cuisines (vendor_id, cuisine_name, description, price_per_head, dietary_flags, service_styles, min_guests, max_guests, active)
SELECT id, 'Dim Sum, Bao & Ramen Bento Box', 'Crystal dumplings, BBQ chicken bao, spicy miso ramen broth with condiments', 460.00, 'HALAL,NON_VEG', 'PLATED,FAMILY_STYLE', 10, 150, 1 FROM users WHERE email = 'wok.artisan@smartcatering.com';
INSERT INTO vendor_cuisines (vendor_id, cuisine_name, description, price_per_head, dietary_flags, service_styles, min_guests, max_guests, active)
SELECT id, 'Thai Green Curry & Jasmine Rice', 'Authentic coconut milk curry with bamboo shoots, baby corn, and fragrant rice', 390.00, 'VEGETARIAN,GLUTEN_FREE', 'PLATED', 10, 120, 1 FROM users WHERE email = 'wok.artisan@smartcatering.com';

-- Shree Swaminarayan Veg
INSERT INTO vendor_cuisines (vendor_id, cuisine_name, description, price_per_head, dietary_flags, service_styles, min_guests, max_guests, active)
SELECT id, 'Royal Gujarati Wedding Rasoi', 'Undhiyu, Puri, Gujarati Kadi, Khichdi, Mohanthal, Basundi, and 5 farsans', 650.00, 'VEGETARIAN,JAIN', 'BUFFET,LIVE_STATION', 40, 800, 1 FROM users WHERE email = 'swaminarayan.catering@smartcatering.com';
INSERT INTO vendor_cuisines (vendor_id, cuisine_name, description, price_per_head, dietary_flags, service_styles, min_guests, max_guests, active)
SELECT id, 'Kathiyawadi Village Dhaba Feast', 'Ringan Bharta, Bajra Rotla with white butter, Sev Tameta, Garlic Chutney & Chaas', 450.00, 'VEGETARIAN', 'FAMILY_STYLE,BUFFET', 25, 400, 1 FROM users WHERE email = 'swaminarayan.catering@smartcatering.com';
INSERT INTO vendor_cuisines (vendor_id, cuisine_name, description, price_per_head, dietary_flags, service_styles, min_guests, max_guests, active)
SELECT id, 'Shuddha Jain Satvik Thali', 'Strict no-root vegetables preparation with utmost purity and authentic spices', 480.00, 'JAIN,VEGETARIAN', 'PLATED,BUFFET', 15, 300, 1 FROM users WHERE email = 'swaminarayan.catering@smartcatering.com';

-- Bella Italia
INSERT INTO vendor_cuisines (vendor_id, cuisine_name, description, price_per_head, dietary_flags, service_styles, min_guests, max_guests, active)
SELECT id, 'Artisanal Wood-Fired Pizza & Pasta Station', 'Hand-stretched sourdough pizzas, live tortellini & penne arrabbiata pasta station', 880.00, 'VEGETARIAN,NON_VEG', 'LIVE_STATION,BUFFET', 20, 250, 1 FROM users WHERE email = 'bella.italia@smartcatering.com';
INSERT INTO vendor_cuisines (vendor_id, cuisine_name, description, price_per_head, dietary_flags, service_styles, min_guests, max_guests, active)
SELECT id, 'Tuscan Truffle Risotto & Antipasto Feast', 'Wild mushroom risotto, focaccia bar, bruschetta platter, and tiramisu', 950.00, 'VEGETARIAN', 'BUFFET,PLATED', 15, 200, 1 FROM users WHERE email = 'bella.italia@smartcatering.com';

-- Royal Mithai Confectioners
INSERT INTO vendor_cuisines (vendor_id, cuisine_name, description, price_per_head, dietary_flags, service_styles, min_guests, max_guests, active)
SELECT id, 'Live Royal Jalebi & Rabdi Dessert Station', 'Crispy saffron jalebis prepared live with thick rabdi, pista garnish, and kulfi', 260.00, 'VEGETARIAN', 'LIVE_STATION', 30, 600, 1 FROM users WHERE email = 'royal.mithai@smartcatering.com';
INSERT INTO vendor_cuisines (vendor_id, cuisine_name, description, price_per_head, dietary_flags, service_styles, min_guests, max_guests, active)
SELECT id, 'Gourmet French Petit Four & Pastry Bar', 'Mini opera cakes, chocolate eclairs, fruit tarts, macarons, and mousse shooters', 480.00, 'VEGETARIAN', 'BUFFET', 20, 350, 1 FROM users WHERE email = 'royal.mithai@smartcatering.com';

-- 7. INSERT REALISTIC CATERING EVENTS (24 Events across Service Types & Statuses)
-- Corporate Event 1: TCS Annual Leadership Dinner
INSERT INTO catering_events (customer_id, vendor_id, cuisine_id, service_type, event_date, occasion_type, guest_count, event_time_from, event_time_to, delivery_address, pickup_time, special_notes, status, staff_consumed, utensils_consumed, vehicle_consumed, venue_covers_consumed, created_at)
SELECT c.id, v.id, (SELECT id FROM vendor_cuisines WHERE vendor_id = v.id LIMIT 1), 'DELIVERY_WITH_SERVING', '2026-10-18', 'CORPORATE', 120, '19:00', '23:00', 'TCS Auditorium, Yantra Park, Thane West, Mumbai - 400606', NULL, 'Executive dinner with CEO and VP in attendance. Professional dress code.', 'CONFIRMED', 6, 132, 1, 0, NOW() - INTERVAL 4 DAY
FROM users c, users v WHERE c.email = 'vikram.mehta@tcs.com' AND v.email = 'royal.feasts@smartcatering.com';

-- Corporate Event 2: Infosys Hackathon Lunch Boxes
INSERT INTO catering_events (customer_id, vendor_id, cuisine_id, service_type, event_date, occasion_type, guest_count, event_time_from, event_time_to, delivery_address, pickup_time, special_notes, status, staff_consumed, utensils_consumed, vehicle_consumed, venue_covers_consumed, created_at)
SELECT c.id, v.id, (SELECT id FROM vendor_cuisines WHERE vendor_id = v.id AND cuisine_name LIKE '%Dim Sum%' LIMIT 1), 'DELIVERY_ONLY', '2026-10-15', 'CORPORATE', 80, '12:30', '14:30', 'Infosys Tower, 4th Floor Cafeteria, Airoli Mindspace, Navi Mumbai', NULL, 'Individual hot sealed bento boxes with labeled dietary tags.', 'CONFIRMED', 0, 80, 1, 0, NOW() - INTERVAL 2 DAY
FROM users c, users v WHERE c.email = 'ananya.sen@infosys.com' AND v.email = 'wok.artisan@smartcatering.com';

-- Wedding Event 3: Rohit Verma Sangeet & Reception at Venue
INSERT INTO catering_events (customer_id, vendor_id, cuisine_id, service_type, event_date, occasion_type, guest_count, event_time_from, event_time_to, delivery_address, pickup_time, special_notes, status, staff_consumed, utensils_consumed, vehicle_consumed, venue_covers_consumed, created_at)
SELECT c.id, v.id, (SELECT id FROM vendor_cuisines WHERE vendor_id = v.id LIMIT 1), 'AT_VENUE', '2026-10-22', 'WEDDING', 150, '18:30', '23:30', NULL, NULL, 'Grand Sangeet night banquet with live starter counters and royal main course.', 'CONFIRMED', 8, 165, 0, 150, NOW() - INTERVAL 6 DAY
FROM users c, users v WHERE c.email = 'rohit.verma@gmail.com' AND v.email = 'royal.feasts@smartcatering.com';

-- Takeaway Event 4: Sneha Kapoor Weekend Party Pickup
INSERT INTO catering_events (customer_id, vendor_id, cuisine_id, service_type, event_date, occasion_type, guest_count, event_time_from, event_time_to, delivery_address, pickup_time, special_notes, status, staff_consumed, utensils_consumed, vehicle_consumed, venue_covers_consumed, created_at)
SELECT c.id, v.id, (SELECT id FROM vendor_cuisines WHERE vendor_id = v.id AND cuisine_name LIKE '%Pizza%' LIMIT 1), 'TAKEAWAY', '2026-10-15', 'BIRTHDAY', 25, NULL, NULL, NULL, '19:30', 'Hot insulated party box collection for evening house party.', 'CONFIRMED', 0, 0, 0, 0, NOW() - INTERVAL 1 DAY
FROM users c, users v WHERE c.email = 'sneha.kapoor@outlook.com' AND v.email = 'bella.italia@smartcatering.com';

-- Corporate Event 5: Nexus Labs Seed Funding Celebration
INSERT INTO catering_events (customer_id, vendor_id, cuisine_id, service_type, event_date, occasion_type, guest_count, event_time_from, event_time_to, delivery_address, pickup_time, special_notes, status, staff_consumed, utensils_consumed, vehicle_consumed, venue_covers_consumed, created_at)
SELECT c.id, v.id, (SELECT id FROM vendor_cuisines WHERE vendor_id = v.id AND cuisine_name LIKE '%Seafood%' LIMIT 1), 'DELIVERY_WITH_SERVING', '2026-10-19', 'SOCIAL', 50, '19:00', '22:00', 'WeWork Enam Sambhav, 5th Floor Terrace, BKC, Mumbai', NULL, 'Celebratory dinner with live grilling and beverage counter.', 'CONFIRMED', 4, 55, 1, 0, NOW() - INTERVAL 3 DAY
FROM users c, users v WHERE c.email = 'amit.sharma@nexuslabs.in' AND v.email = 'coastal.curries@smartcatering.com';

-- Corporate Event 6: Deloitte Partner Quarterly Meeting
INSERT INTO catering_events (customer_id, vendor_id, cuisine_id, service_type, event_date, occasion_type, guest_count, event_time_from, event_time_to, delivery_address, pickup_time, special_notes, status, staff_consumed, utensils_consumed, vehicle_consumed, venue_covers_consumed, created_at)
SELECT c.id, v.id, (SELECT id FROM vendor_cuisines WHERE vendor_id = v.id AND cuisine_name LIKE '%Tuscan%' LIMIT 1), 'DELIVERY_WITH_SERVING', '2026-10-20', 'CONFERENCE', 40, '12:00', '15:00', 'Deloitte Center, Boardroom 1, Senapati Bapat Marg, Lower Parel', NULL, 'Plated executive lunch with fine cutlery and linen napkins.', 'CONFIRMED', 3, 44, 1, 0, NOW() - INTERVAL 2 DAY
FROM users c, users v WHERE c.email = 'neha.gupta@deloitte.com' AND v.email = 'bella.italia@smartcatering.com';

-- Religious Event 7: Kavita Nair Diwali Vastu Puja
INSERT INTO catering_events (customer_id, vendor_id, cuisine_id, service_type, event_date, occasion_type, guest_count, event_time_from, event_time_to, delivery_address, pickup_time, special_notes, status, staff_consumed, utensils_consumed, vehicle_consumed, venue_covers_consumed, created_at)
SELECT c.id, v.id, (SELECT id FROM vendor_cuisines WHERE vendor_id = v.id AND cuisine_name LIKE '%Gujarati%' LIMIT 1), 'DELIVERY_WITH_SERVING', '2026-10-24', 'RELIGIOUS', 100, '11:00', '15:00', '12 Rosewood Villa, Deonar Farm Road, Chembur, Mumbai', NULL, 'Strict Jain and Satvik prep required for household temple puja.', 'CONFIRMED', 5, 110, 1, 0, NOW() - INTERVAL 5 DAY
FROM users c, users v WHERE c.email = 'kavita.nair@gmail.com' AND v.email = 'swaminarayan.catering@smartcatering.com';

-- Corporate Event 8: Zomato Friday Feast Buffet
INSERT INTO catering_events (customer_id, vendor_id, cuisine_id, service_type, event_date, occasion_type, guest_count, event_time_from, event_time_to, delivery_address, pickup_time, special_notes, status, staff_consumed, utensils_consumed, vehicle_consumed, venue_covers_consumed, created_at)
SELECT c.id, v.id, (SELECT id FROM vendor_cuisines WHERE vendor_id = v.id AND cuisine_name LIKE '%Jalebi%' LIMIT 1), 'DELIVERY_WITH_SERVING', '2026-10-17', 'CORPORATE', 200, '16:00', '18:30', 'One International Center, 9th Floor Townhall, Elphinstone Road', NULL, 'Live evening dessert buffet for all teams.', 'CONFIRMED', 6, 220, 1, 0, NOW() - INTERVAL 1 DAY
FROM users c, users v WHERE c.email = 'rahul.bhatia@zomato.com' AND v.email = 'royal.mithai@smartcatering.com';

-- Event 9: Pending Wedding Catering Enquiry
INSERT INTO catering_events (customer_id, vendor_id, cuisine_id, service_type, event_date, occasion_type, guest_count, event_time_from, event_time_to, delivery_address, pickup_time, special_notes, status, staff_consumed, utensils_consumed, vehicle_consumed, venue_covers_consumed, created_at)
SELECT c.id, v.id, (SELECT id FROM vendor_cuisines WHERE vendor_id = v.id LIMIT 1), 'AT_VENUE', '2026-10-28', 'WEDDING', 120, '19:00', '23:30', NULL, NULL, 'Prospective wedding sangeet dinner banquet.', 'PENDING', 6, 132, 0, 120, NOW() - INTERVAL 10 HOUR
FROM users c, users v WHERE c.email = 'rohit.verma@gmail.com' AND v.email = 'swaminarayan.catering@smartcatering.com';

-- Event 10: Pending Takeaway Order
INSERT INTO catering_events (customer_id, vendor_id, cuisine_id, service_type, event_date, occasion_type, guest_count, event_time_from, event_time_to, delivery_address, pickup_time, special_notes, status, staff_consumed, utensils_consumed, vehicle_consumed, venue_covers_consumed, created_at)
SELECT c.id, v.id, (SELECT id FROM vendor_cuisines WHERE vendor_id = v.id AND cuisine_name LIKE '%Curry%' LIMIT 1), 'TAKEAWAY', '2026-10-16', 'OTHER', 15, NULL, NULL, NULL, '20:15', 'Quick family dinner meal box pickup.', 'PENDING', 0, 0, 0, 0, NOW() - INTERVAL 3 HOUR
FROM users c, users v WHERE c.email = 'sneha.kapoor@outlook.com' AND v.email = 'wok.artisan@smartcatering.com';

-- Event 11: Past Completed Event (Last week)
INSERT INTO catering_events (customer_id, vendor_id, cuisine_id, service_type, event_date, occasion_type, guest_count, event_time_from, event_time_to, delivery_address, pickup_time, special_notes, status, staff_consumed, utensils_consumed, vehicle_consumed, venue_covers_consumed, created_at)
SELECT c.id, v.id, (SELECT id FROM vendor_cuisines WHERE vendor_id = v.id LIMIT 1), 'DELIVERY_ONLY', '2026-10-10', 'CORPORATE', 60, '13:00', '15:00', 'TCS Yantra Park, Thane West', NULL, 'Client lunch buffet delivery.', 'CONFIRMED', 0, 60, 1, 0, NOW() - INTERVAL 8 DAY
FROM users c, users v WHERE c.email = 'vikram.mehta@tcs.com' AND v.email = 'royal.feasts@smartcatering.com';

-- Event 12: Cancelled Booking
INSERT INTO catering_events (customer_id, vendor_id, cuisine_id, service_type, event_date, occasion_type, guest_count, event_time_from, event_time_to, delivery_address, pickup_time, special_notes, status, staff_consumed, utensils_consumed, vehicle_consumed, venue_covers_consumed, created_at)
SELECT c.id, v.id, (SELECT id FROM vendor_cuisines WHERE vendor_id = v.id LIMIT 1), 'DELIVERY_WITH_SERVING', '2026-10-14', 'SOCIAL', 40, '18:00', '21:00', 'Worli Sea Face Apartment Banquet', NULL, 'Postponed due to monsoon weather forecast.', 'CANCELLED', 0, 0, 0, 0, NOW() - INTERVAL 5 DAY
FROM users c, users v WHERE c.email = 'rohit.verma@gmail.com' AND v.email = 'coastal.curries@smartcatering.com';

-- 8. INSERT CATERING SLOTS FOR SLOTS DIRECTORY
INSERT INTO catering_slot (vendor_id, date, time_slot, capacity, slots_booked, menu_type)
SELECT id, '2026-10-16', '12:00 PM - 03:00 PM', 150, 45, 'Royal Rajasthani Thali' FROM users WHERE email = 'royal.feasts@smartcatering.com';

INSERT INTO catering_slot (vendor_id, date, time_slot, capacity, slots_booked, menu_type)
SELECT id, '2026-10-16', '07:30 PM - 11:00 PM', 200, 120, 'Shahi Rajputana Grand Buffet' FROM users WHERE email = 'royal.feasts@smartcatering.com';

INSERT INTO catering_slot (vendor_id, date, time_slot, capacity, slots_booked, menu_type)
SELECT id, '2026-10-17', '01:00 PM - 04:00 PM', 100, 30, 'Coastal Fish & Prawn Curry Feast' FROM users WHERE email = 'coastal.curries@smartcatering.com';

INSERT INTO catering_slot (vendor_id, date, time_slot, capacity, slots_booked, menu_type)
SELECT id, '2026-10-17', '07:00 PM - 10:30 PM', 120, 95, 'Authentic Kerala Sadya' FROM users WHERE email = 'coastal.curries@smartcatering.com';

INSERT INTO catering_slot (vendor_id, date, time_slot, capacity, slots_booked, menu_type)
SELECT id, '2026-10-18', '12:30 PM - 03:30 PM', 250, 180, 'Gujarati Grand Wedding Buffet' FROM users WHERE email = 'swaminarayan.catering@smartcatering.com';

INSERT INTO catering_slot (vendor_id, date, time_slot, capacity, slots_booked, menu_type)
SELECT id, '2026-10-18', '07:30 PM - 11:30 PM', 80, 50, 'Italian Wood-Fired Pizza & Pasta' FROM users WHERE email = 'bella.italia@smartcatering.com';

-- 9. INSERT LINKED BOOKINGS
INSERT INTO booking (user_id, slot_id, number_of_guests, status, created_at)
SELECT c.id, (SELECT id FROM catering_slot WHERE menu_type LIKE '%Rajasthani%' LIMIT 1), 35, 'CONFIRMED', NOW() - INTERVAL 2 DAY
FROM users c WHERE c.email = 'vikram.mehta@tcs.com';

INSERT INTO booking (user_id, slot_id, number_of_guests, status, created_at)
SELECT c.id, (SELECT id FROM catering_slot WHERE menu_type LIKE '%Kerala%' LIMIT 1), 25, 'CONFIRMED', NOW() - INTERVAL 1 DAY
FROM users c WHERE c.email = 'ananya.sen@infosys.com';

INSERT INTO booking (user_id, slot_id, number_of_guests, status, created_at)
SELECT c.id, (SELECT id FROM catering_slot WHERE menu_type LIKE '%Italian%' LIMIT 1), 15, 'CONFIRMED', NOW() - INTERVAL 12 HOUR
FROM users c WHERE c.email = 'sneha.kapoor@outlook.com';

INSERT INTO booking (user_id, slot_id, number_of_guests, status, created_at)
SELECT c.id, (SELECT id FROM catering_slot WHERE menu_type LIKE '%Gujarati%' LIMIT 1), 50, 'CONFIRMED', NOW() - INTERVAL 3 DAY
FROM users c WHERE c.email = 'amit.sharma@nexuslabs.in';
