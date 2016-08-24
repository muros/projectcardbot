insert into user(id, username, first_name, last_name, created_date, location, max_limit) values ('11111111-1111-1111-1111-111111111111', 'danveloper', 'Dan', 'Woods', now(), 'US', 100.00);
insert into user(id, username, first_name, last_name, created_date, location, max_limit) values ('22222222-2222-2222-2222-222222222222', 'muros', 'Uroš', 'Mesarič', now(), 'SI', 300.42);

insert into country(code, name) values ('US', 'United States');
insert into country(code, name) values ('UK', 'United Kingdom');
insert into country(code, name) values ('SI', 'Slovenia');

insert into gift_provider(id, name) values ('gyft', 'Gyft cards.');

insert into gift_provider_country(id, code) values ('gyft', 'US');
insert into gift_provider_country(id, code) values ('gyft', 'UK');