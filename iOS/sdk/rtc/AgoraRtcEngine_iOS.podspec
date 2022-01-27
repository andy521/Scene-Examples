Pod::Spec.new do |s|
  s.name             = 'AgoraRtcEngine_iOS'
  s.version          = '3.6.200.206'
  s.summary          = 'AgoraRtcEngine_iOS'
  
  s.description      = <<-DESC
  TODO: Add long description of the pod here.
  DESC
  
  s.homepage         = 'https://github.com'
  s.license          = { :type => 'MIT', :file => 'LICENSE' }
  s.author           = { 'ZYP' => 'xxx@agora.io' }
  s.source           = { :git => 'https://github.com', :tag => s.version.to_s }
  
  s.ios.deployment_target = '11.0'
  s.vendored_frameworks = ["*.xcframework", "*.framework"]
  s.platform = :ios
end
