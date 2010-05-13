-- Notes
-- You will need to define the languages. Replace 'English'
-- With your language1 and Swahili with your language2
-- As defined in the config files

CREATE TABLE "dict" (
  "Id" integer NOT NULL primary key autoincrement,
  "PartOfSpeech" integer unsigned NOT NULL default '0',
  "Class" integer unsigned NOT NULL default '0',
  "SwahiliSortBy" varchar(16) NOT NULL default '',
  "EnglishSortBy" varchar(17) NOT NULL default '',
  "SwahiliWord" varchar(53) NOT NULL default '',
  "EnglishWord" varchar(175) NOT NULL default '',
  "SwahiliPlural" varchar(255) NOT NULL default '',
  "EnglishPlural" varchar(255) NOT NULL default '',
  "SwahiliDefinition" varchar(174) NOT NULL default '',
  "SwahiliExample" text,
  "EnglishExample" text,
  "Derived" varchar(82) NOT NULL default '',
  "DialectNote" varchar(255) NOT NULL default '',
  "Dialect" text NOT NULL,
  "Terminology" text NOT NULL,
  "EnglishDef" text,
  "DerivedLang" integer unsigned default NULL,
  "Taxonomy" text NOT NULL,
  "RelatedWords" text NOT NULL,
  "EngAlt" varchar(255) NOT NULL default '',
  "SwaAlt" varchar(255) NOT NULL default '',
  "EngPluralAlt" varchar(255) NOT NULL default '',
  "SwaPluralAlt" varchar(255) NOT NULL default ''
);
CREATE TABLE "word_grouping" (
  "SortBy" varchar(255) NOT NULL default '',
  "EngP" integer unsigned NOT NULL default '0',
  "GroupNum" integer unsigned default NULL,
  "InGroupPos" integer unsigned default NULL,
  "WordId" integer unsigned default NULL
);