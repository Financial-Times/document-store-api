@BodyTransformation
Feature: Body transformation

  Scenario: Simple substitution
    Given I have a url template mapping of type http://www.ft.com/ontology/content/Article to url /content/{{id}}
	And I have body text in the article consisting of <body><content id="abc123" type="http://www.ft.com/ontology/content/Article">...</content></body>
	When I transform the article for output    
    Then the body text in the article should have been transformed into <body><ft-content type="http://www.ft.com/ontology/content/Article" url="http://localhost/content/abc123">...</ft-content></body>

  Scenario: Multiple substitution
    Given I have a url template mapping of type http://www.ft.com/ontology/content/Article to url /content/{{id}}
	And I have body text in the article consisting of <body><content id="abc123" type="http://www.ft.com/ontology/content/Article">...</content><content id="def456" type="http://www.ft.com/ontology/content/Article">...</content></body>
	When I transform the article for output    
    Then the body text in the article should have been transformed into <body><ft-content type="http://www.ft.com/ontology/content/Article" url="http://localhost/content/abc123">...</ft-content><ft-content type="http://www.ft.com/ontology/content/Article" url="http://localhost/content/def456">...</ft-content></body>

  Scenario: Nested substitution
    Given I have a url template mapping of type http://www.ft.com/ontology/content/Article to url /content/{{id}}
	And I have body text in the article consisting of <body><content id="abc123" type="http://www.ft.com/ontology/content/Article"><content id="def456" type="http://www.ft.com/ontology/content/Article">...</content></content></body>
	When I transform the article for output it will fail

  Scenario: Missing ID
    Given I have a url template mapping of type http://www.ft.com/ontology/content/Article to url /content/{{id}}
	And I have body text in the article consisting of <body><content type="http://www.ft.com/ontology/content/Article">...</content></body>
	When I transform the article for output it will fail

  Scenario: Missing type
    Given I have a url template mapping of type http://www.ft.com/ontology/content/Article to url /content/{{id}}
	And I have body text in the article consisting of <body><content id="abc123">...</content></body>
	When I transform the article for output it will fail

  Scenario: Bad template
    Given I have a url template mapping of type http://www.ft.com/ontology/content/Article to url /content/{{qid}}
	And I have body text in the article consisting of <body><content id="abc123" type="http://www.ft.com/ontology/content/Article">...</content></body>
	When I transform the article for output it will fail

  Scenario: Missing template
	Given I have body text in the article consisting of <body><content id="abc123" type="http://www.ft.com/ontology/content/Article">...</content></body>
	When I transform the article for output it will fail

  Scenario: Retain additional attributes
    Given I have a url template mapping of type http://www.ft.com/ontology/content/Article to url /content/{{id}}
	And I have body text in the article consisting of <body><content id="abc123" type="http://www.ft.com/ontology/content/Article" title="The title">...</content></body>
	When I transform the article for output    
    Then the body text in the article should have been transformed into <body><ft-content type="http://www.ft.com/ontology/content/Article" url="http://localhost/content/abc123" title="The title">...</ft-content></body>

  Scenario: Handle inline images
    Given I have a url template mapping of type http://www.ft.com/ontology/content/ImageSet to url /content/{{id}}
    And I have body text in the article consisting of <body><content data-embedded="true" id="abc123" type="http://www.ft.com/ontology/content/ImageSet">...</content></body>
    When I transform the article for output
    Then the body text in the article should have been transformed into <body><ft-content type="http://www.ft.com/ontology/content/ImageSet" url="http://localhost/content/abc123" data-embedded="true">...</ft-content></body>

  Scenario: Handle related items and related content promoboxes
    Given I have a url template mapping of type http://www.ft.com/ontology/content/Article to url /content/{{id}}
    And I have body text in the article consisting of <body><related id="abc123" type="http://www.ft.com/ontology/content/Article">...</related></body>
    When I transform the article for output
    Then the body text in the article should have been transformed into <body><ft-related type="http://www.ft.com/ontology/content/Article" url="http://localhost/content/abc123">...</ft-related></body>

  Scenario: Handle related content promobox with image
    Given I have a url template mapping of type http://www.ft.com/ontology/content/Article to url /content/{{id}}
    And I have a url template mapping of type http://www.ft.com/ontology/content/ImageSet to url /content/{{id}}
    And I have body text in the article consisting of <body><related id="abc123" type="http://www.ft.com/ontology/content/Article"><media><content data-embedded="true" id="abc123" type="http://www.ft.com/ontology/content/ImageSet">...</content></media></related></body>
    When I transform the article for output
    Then the body text in the article should have been transformed into <body><ft-related type="http://www.ft.com/ontology/content/Article" url="http://localhost/content/abc123"><media><ft-content type="http://www.ft.com/ontology/content/ImageSet" url="http://localhost/content/abc123" data-embedded="true">...</ft-content></media></ft-related></body>
